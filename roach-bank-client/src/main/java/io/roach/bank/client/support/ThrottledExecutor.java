package io.roach.bank.client.support;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import io.roach.bank.client.util.MethodStats;

@Component
public class ThrottledExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, LinkedList<Future<String>>> workers
            = Collections.synchronizedMap(new LinkedHashMap<>());

    private final Map<String, ReentrantLock> locks
            = Collections.synchronizedMap(new LinkedHashMap<>());

    private ThreadPoolExecutor executor;

    @Value("${roachbank.threadPool.corePoolSize}")
    private int poolSize;

    @Value("${roachbank.threadPool.blockingCoef}")
    private int blockingCoef;

    @PostConstruct
    public void init() {
        int corePoolSize = poolSize > 0 ? poolSize
                : Runtime.getRuntime().availableProcessors() * (1 + blockingCoef);

        this.executor = new ThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(corePoolSize * 2));

        logger.info("Bootstrap ThrottledExecutor with core pool size: {} available vcpus: {}",
                corePoolSize, Runtime.getRuntime().availableProcessors());
    }

    @PreDestroy
    public void teardown() {
        try {
            cancelAllWorkers();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public <V> Future<String> submit(Callable<V> callable, Duration duration, String groupName) {
        return submit(callable, duration, groupName, s -> {
        });
    }

    public <V> Future<String> submit(Callable<V> callable, Duration duration, String groupName,
                                     Consumer<String> completionCallback) {
        final Supplier<String> displayNameCallback =
                () -> groupName + " (" + (workers.getOrDefault(groupName, new LinkedList<>()).size()) + ")";

        // Exponential backoff mutex at group level
        locks.computeIfAbsent(groupName, k -> new ReentrantLock());

        final MethodStats methodStats = MethodStats.of(groupName, displayNameCallback, duration);

        final Future<String> future = executor.submit(() -> {
            final long startTime = System.currentTimeMillis();

            long callCount = 0;

            do {
                ReentrantLock lock = locks.get(groupName);

                try {
                    if (lock.isLocked()) {
                        if (lock.tryLock(30, TimeUnit.SECONDS)) {
                            lock.unlock();
                        }
                    } else {
                        methodStats.beforeMethod();
                        callCount++;

                        callable.call();

                        methodStats.afterMethod(null);
                        callCount = 0;
                    }
                } catch (HttpStatusCodeException | ResourceAccessException e) { // Retry on all HTTP errors
                    methodStats.afterMethod(e);

                    boolean locked = false;
                    try {
                        // Grab lock to pause other threads against same worker group
                        locked = lock.tryLock();

                        long backoffMillis = Math.min((long) (Math.pow(2, callCount) + Math.random() * 1000), 15000);
                        if (logger.isWarnEnabled()) {
                            if (e instanceof HttpStatusCodeException) {
                                HttpStatus status = ((HttpStatusCodeException) e).getStatusCode();
                                logger.warn("HTTP status {} (backoff {}ms) in call #{} to '{}': {}",
                                        status, backoffMillis, callCount, groupName, e.getMessage());
                            } else {
                                logger.warn("HTTP resource exception (backoff {}ms) in call #{} to '{}': {}",
                                        backoffMillis, callCount, groupName, e.getMessage());
                            }
                        }
                        Thread.sleep(backoffMillis);
                    } finally {
                        if (locked) {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.trace("Thread interrupted - aborting worker for {} prematurely", groupName);
                    break;
                } catch (Throwable e) {
                    logger.error("Uncategorized error - aborting worker prematurely", e);
                    break;
                }
            } while (System.currentTimeMillis() - startTime < duration.toMillis()
                    && !executor.isTerminating()
                    && !executor.isShutdown()
                    && !Thread.interrupted()
            );

            return displayNameCallback.get();
        });

        final LinkedList<Future<String>> futures = workers.computeIfAbsent(groupName, k -> new LinkedList<>());

        if (futures.isEmpty()) {
            futures.add(future);

            executor.submit(() -> {
                while (!futures.isEmpty()) {
                    Future<String> f = futures.peek();
                    try {
                        f.get();
                        logger.info("Worker group '{}' finished", groupName);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        logger.warn("Worker group '" + groupName + "' error", e);
                    } catch (CancellationException e) {
                        logger.debug("Worker group '{}' cancelled", groupName);
                    } finally {
                        futures.remove(f);
                        completionCallback.accept(groupName);
                    }
                }
                workers.remove(groupName);
                MethodStats.remove(groupName);
            });
        } else {
            futures.add(future);
            logger.debug("Adding worker for group '{}'", groupName);
        }

        return future;
    }

    public void cancelWorkers(String groupName) {
        cancelFutures(workers.getOrDefault(groupName, new LinkedList<>()));
    }

    public void cancelAllWorkers() {
        workers.forEach((name, futures) -> cancelFutures(futures));
        workers.clear();
        executor.purge();
    }

    private void cancelFutures(LinkedList<Future<String>> futures) {
        while (!futures.isEmpty()) {
            futures.poll().cancel(true);
        }
    }

    public int activeWorkerCount() {
        return executor.getActiveCount();
    }

    public void printStatus() {
        logger.debug(">> Executor Stats");
        logger.debug("activeCount: {}", executor.getActiveCount());
        logger.debug("taskCount: {}", executor.getTaskCount());
        logger.debug("completedTaskCount: {}", executor.getCompletedTaskCount());
        logger.debug("corePoolSize: {}", executor.getCorePoolSize());
        logger.debug("poolSize: {}", executor.getPoolSize());
        logger.debug("largestPoolSize: {}", executor.getLargestPoolSize());
        logger.debug("maximumPoolSize: {}", executor.getMaximumPoolSize());
        logger.debug("isShutdown: {}", executor.isShutdown());
        logger.debug("isTerminated: {}", executor.isTerminated());
        logger.debug("isTerminating: {}", executor.isTerminating());
        logger.debug("queue: {}:", executor.getQueue().size());
        logger.debug("workerCount: {}:", workers.size());
    }
}
