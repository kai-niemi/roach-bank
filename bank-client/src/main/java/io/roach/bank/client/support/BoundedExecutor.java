package io.roach.bank.client.support;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoundedExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, AtomicInteger> workers = new ConcurrentHashMap<>();

    private final Map<String, Semaphore> throttle = new ConcurrentHashMap<>();

    private final CallMetric callMetric;

    private int corePoolSize;

    private ExecutorService executorService;

    public BoundedExecutor(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        this.callMetric = new CallMetric();
        this.executorService = createExecutorService(corePoolSize);
    }

    private ExecutorService createExecutorService(int poolSize) {
        return new ThreadPoolExecutor(poolSize,
                Integer.MAX_VALUE,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>());
    }

    public CallMetric getCallMetric() {
        return callMetric;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public <V> Future<V> submit(final Callable<V> task, final String groupName, int queueSize) {
        Semaphore semaphore = throttle.computeIfAbsent(groupName,
                s -> new Semaphore(queueSize <= 0 ? Runtime.getRuntime().availableProcessors() : queueSize));

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to acquire semaphore", e);
        }

        try {
            return executorService.submit(() -> {
                AtomicInteger concurrency = workers.computeIfAbsent(groupName, i -> new AtomicInteger());
                concurrency.incrementAndGet();

                CallMetric.Context metric = callMetric.of(groupName, concurrency::get);

                final long startTime = metric.enter();
                try {
                    V v = task.call();
                    metric.exit(startTime, null);
                    return v;
                } catch (Exception e) {
                    metric.exit(startTime, e);
                    logger.error("Execution error", e);
                    throw new UndeclaredThrowableException(e);
                } finally {
                    concurrency.decrementAndGet();
                    semaphore.release();
                }
            });
        } catch (RejectedExecutionException e) {
            semaphore.release();
            return null;
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void cancelAndRestart(int corePoolSize) {
        throttle.values().forEach(semaphore -> {
            if (semaphore.hasQueuedThreads()) {
                semaphore.drainPermits();
            }
        });
        this.corePoolSize = corePoolSize;

        cancelAndRestart();
    }

    public void cancelAndRestart() {
        this.executorService.shutdownNow();

        try {
            while (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                if (activeWorkers() > 0) {
                    logger.info("Cancelling {} active workers - awaiting completion", activeWorkers());
                }
            }

            if (!this.executorService.isTerminated()) {
                throw new IllegalStateException();
            }

            this.workers.clear();
            this.callMetric.clear();

            logger.info("Creating new thread pool with size {}", corePoolSize);

            this.throttle.clear();
            this.executorService = createExecutorService(corePoolSize);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public boolean hasActiveWorkers() {
        AtomicBoolean b = new AtomicBoolean();
        throttle.values().forEach(semaphore -> {
            if (semaphore.hasQueuedThreads()) {
                b.set(true);
            }
        });
        return activeWorkers() > 0 || b.get();
    }

    private int activeWorkers() {
        return workers.values().stream().mapToInt(AtomicInteger::get).sum();
    }
}
