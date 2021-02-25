package io.roach.bank.client.support;

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
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class ThrottledExecutor {
    private final Map<String, LinkedList<Future<String>>> workers
            = Collections.synchronizedMap(new LinkedHashMap<>());

    private final Map<String, ReentrantLock> locks
            = Collections.synchronizedMap(new LinkedHashMap<>());

    private ThreadPoolExecutor executor;

    @Value("${roachbank.threadPool.corePoolSize}")
    private int poolSize;

    @Value("${roachbank.threadPool.blockingCoef}")
    private int blockingCoef;

    @Autowired
    private Console console;

    @PostConstruct
    public void init() {
        int corePoolSize = poolSize > 0 ? poolSize
                : Runtime.getRuntime().availableProcessors() * (1 + blockingCoef);

        this.executor = new ThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(corePoolSize * 2));

        console.info("Bootstrap ThrottledExecutor with core pool size: %d available vcpus: %d",
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

    public <V> Future<String> submit(Callable<V> callable, TaskDuration duration, String groupName) {
        final Supplier<String> displayNameCallback =
                () -> groupName + " (" + (workers.getOrDefault(groupName, new LinkedList<>()).size()) + ")";

        // Exponential backoff mutex at group level
        locks.computeIfAbsent(groupName, k -> new ReentrantLock());

        final CallStats callStats = CallStats.of(groupName, displayNameCallback, duration);

        final Future<String> future = executor.submit(() -> {
            final long startTime = System.currentTimeMillis();

            long callCount = 0;

            do {
                final ReentrantLock lock = locks.get(groupName);
                final long begin = callStats.now();

                try {
                    if (lock.isLocked()) {
                        if (lock.tryLock(30, TimeUnit.SECONDS)) {
                            lock.unlock();
                        }
                    } else {
                        callCount++;

                        callable.call();

                        callStats.mark(begin, null);
                        callCount = 0;
                    }
                } catch (HttpStatusCodeException | ResourceAccessException e) { // Retry on all HTTP errors
                    callStats.mark(begin, e);

                    boolean locked = false;
                    try {
                        // Grab lock to pause other threads against same worker group
                        locked = lock.tryLock();

                        long backoffMillis = Math.min((long) (Math.pow(2, callCount) + Math.random() * 1000), 15000);
                        if (e instanceof HttpStatusCodeException) {
                            HttpStatus status = ((HttpStatusCodeException) e).getStatusCode();
                            console.warn("HTTP status %s (backoff %dms) in call #%s to '%s': %s",
                                    status, backoffMillis, callCount, groupName, e.getMessage());
                        } else {
                            console.warn("HTTP resource exception (backoff %dms) in call #%s to '%s': %s",
                                    backoffMillis, callCount, groupName, e.getMessage());
                        }
                        Thread.sleep(backoffMillis);
                    } finally {
                        if (locked) {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    console.warn("Thread interrupted - aborting worker for %s prematurely", groupName);
                    break;
                } catch (Throwable e) {
                    console.error("Uncategorized error - aborting worker prematurely", e);
                    break;
                }
            } while (duration.progress()
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
                        console.info("Worker group '%s' finished", groupName);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        console.warn("Worker group '" + groupName + "' error", e);
                    } catch (CancellationException e) {
                        console.debug("Worker group '%s' cancelled", groupName);
                    } finally {
                        futures.remove(f);
                    }
                }
                workers.remove(groupName);
                CallStats.remove(groupName);
            });
        } else {
            futures.add(future);
            console.debug("Adding worker for group '%s'", groupName);
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
        console.debug("Executor Stats:");
        console.debug("activeCount: %d", executor.getActiveCount());
        console.debug("taskCount: %d", executor.getTaskCount());
        console.debug("completedTaskCount: %d", executor.getCompletedTaskCount());
        console.debug("corePoolSize: %d", executor.getCorePoolSize());
        console.debug("poolSize: %d", executor.getPoolSize());
        console.debug("largestPoolSize: %d", executor.getLargestPoolSize());
        console.debug("maximumPoolSize: %d", executor.getMaximumPoolSize());
        console.debug("isShutdown: %d", executor.isShutdown());
        console.debug("isTerminated: %d", executor.isTerminated());
        console.debug("isTerminating: %d", executor.isTerminating());
        console.debug("queue: %d:", executor.getQueue().size());
        console.debug("workerCount: %d:", workers.size());
    }
}
