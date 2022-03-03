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

    private final CallMetrics callMetrics;

    private int corePoolSize;

    private ExecutorService executorService;

    public BoundedExecutor(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        this.callMetrics = new CallMetrics();
        this.executorService = createExecutorService(corePoolSize);
    }

    private ExecutorService createExecutorService(int poolSize) {
        logger.debug("Setting executor pool size {}", poolSize);
        return new ThreadPoolExecutor(poolSize,
                Integer.MAX_VALUE,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>());
    }

    public CallMetrics getCallMetrics() {
        return callMetrics;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public <V> Future<V> submitTask(final Callable<V> task, final String groupName, int queueSize) {
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queueSize <= 0");
        }

        final Semaphore semaphore = throttle.computeIfAbsent(groupName, s -> new Semaphore(queueSize));

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        try {
            return executorService.submit(() -> {
                AtomicInteger activeWorkers = workers.computeIfAbsent(groupName, i -> new AtomicInteger());
                activeWorkers.incrementAndGet();

                CallMetrics.Context context = callMetrics.of(groupName, activeWorkers::get);

                final long startTime = context.enter();
                try {
                    V v = task.call();
                    context.exit(startTime, null);
                    return v;
                } catch (Exception e) {
                    context.exit(startTime, e);
                    logger.error("Execution error", e);
                    throw new UndeclaredThrowableException(e);
                } finally {
                    activeWorkers.decrementAndGet();
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
            logger.info("Cancelling active workers ({})", activeWorkers());

            while (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.info("Cancelling active workers ({})", activeWorkers());
            }

            if (!this.executorService.isTerminated()) {
                throw new IllegalStateException();
            }

            logger.info("Active workers finished");

            this.workers.clear();
            this.callMetrics.clear();
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
