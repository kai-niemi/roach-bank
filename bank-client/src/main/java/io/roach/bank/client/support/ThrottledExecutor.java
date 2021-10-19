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
import java.util.function.Consumer;
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

    private ThreadPoolExecutor executorService;

    @Value("${roachbank.threadPoolSize}")
    private int poolSize;

    @Autowired
    private Console console;

    @PostConstruct
    public void init() {
        int corePoolSize = poolSize > 0 ? poolSize : Runtime.getRuntime().availableProcessors() * 2;
        this.executorService = createThreadPoolExecutor(corePoolSize, corePoolSize * 2);
    }

    private ThreadPoolExecutor createThreadPoolExecutor(int corePoolSize, int queueSize) {
        console.info("Creating thread pool with core pool size: %d, queue size: %d, available vcpus: %d",
                corePoolSize,
                queueSize,
                Runtime.getRuntime().availableProcessors());
        return new ThreadPoolExecutor(corePoolSize,
                Integer.MAX_VALUE,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(queueSize));
    }

    public void cancelAndRestart(int corePoolSize, int queueSize) {
        console.info("Cancelling %d active workers - awaiting completion", activeWorkerCount());
        this.executorService.shutdownNow();

        try {
            while (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                console.info("Cancelling %d active workers - awaiting completion", activeWorkerCount());
            }

            console.info("All workers terminated");

            if (!this.executorService.isTerminated()) {
                throw new IllegalStateException();
            }

            this.workers.clear();

            this.executorService = createThreadPoolExecutor(corePoolSize, queueSize);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void teardown() {
        try {
            cancelAllWorkers();
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
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

        final Future<String> future = executorService.submit(() -> {
            long callCount = 0;

            do {
                final ReentrantLock lock = locks.get(groupName);
                final long beginTime = callStats.now();

                try {
                    if (lock.isLocked()) {
                        if (lock.tryLock(30, TimeUnit.SECONDS)) {
                            lock.unlock();
                        }
                    } else {
                        callCount++;

                        callable.call();

                        callStats.mark(beginTime, null);

                        if (callCount > 1) {
                            console.info("Recovered after %s transient errors in call to '%s'", callCount, groupName);
                        }

                        callCount = 0;
                    }
                } catch (HttpStatusCodeException | ResourceAccessException e) { // Retry on all HTTP errors
                    callStats.mark(beginTime, e);

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
                    && !executorService.isTerminating()
                    && !executorService.isShutdown()
                    && !Thread.interrupted()
            );

            return displayNameCallback.get();
        });

        final LinkedList<Future<String>> futures = workers.computeIfAbsent(groupName, k -> new LinkedList<>());

        if (futures.isEmpty()) {
            futures.add(future);

            executorService.submit(() -> {
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

        executorService.purge();
    }

    private void cancelFutures(LinkedList<Future<String>> futures) {
        while (!futures.isEmpty()) {
            futures.poll().cancel(true);
        }
    }

    public int activeWorkerCount() {
        return executorService.getActiveCount();
    }

    public void printStatus(Consumer<ThreadPoolExecutor> consumer) {
        consumer.accept(executorService);
    }
}
