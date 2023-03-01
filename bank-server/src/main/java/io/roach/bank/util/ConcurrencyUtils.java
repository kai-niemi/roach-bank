package io.roach.bank.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for submitting concurrent workers with a collective timeout and
 * graceful cancellation.
 */
public abstract class ConcurrencyUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtils.class);

    private ConcurrencyUtils() {
    }

    public static <V> V run(Callable<V> task, long timeout, TimeUnit timeUnit)
            throws TimeoutException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<V> future = executor.submit(task);
        executor.shutdown();
        try {
            return future.get(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new UndeclaredThrowableException(t);
            }
        }
    }

    public static <V> void runConcurrentlyAndWait(List<Callable<V>> tasks, long timeout, TimeUnit timeUnit) {
        runConcurrentlyAndWait(tasks, timeout, timeUnit, null);
    }

    public static <V> void runConcurrentlyAndWait(List<Callable<V>> tasks, long timeout, TimeUnit timeUnit,
                                                  Consumer<V> consumer) {
        ScheduledExecutorService cancellation = Executors.newSingleThreadScheduledExecutor();

        ExecutorService execution = new ThreadPoolExecutor(ForkJoinPool.getCommonPoolParallelism(),
                Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(ForkJoinPool.getCommonPoolParallelism()));

        List<CompletableFuture<V>> allFutures = new ArrayList<>();

        final long expirationTime = System.currentTimeMillis() + timeUnit.toMillis(timeout);

        tasks.forEach(callable -> {
            CompletableFuture<V> f = CompletableFuture.supplyAsync(() -> {
                if (System.currentTimeMillis() > expirationTime) {
                    logger.warn("Task scheduled after expiration time: " + callable);
                    return null;
                }
                Future<V> future = execution.submit(callable);
                long delay = Math.abs(expirationTime - System.currentTimeMillis());
                cancellation.schedule(() -> future.cancel(true), delay, TimeUnit.MILLISECONDS);
                try {
                    V result = future.get();
                    if (consumer != null) {
                        consumer.accept(result);
                    }
                    return result;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Task interrupt: " + e);
                } catch (CancellationException e) {
                    logger.warn("Task cancellation: " + e);
                } catch (ExecutionException e) {
                    logger.error("Task fail", e);
                }
                return null;
            });
            allFutures.add(f);
        });

        try {
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[] {})).join();
        } finally {
            if (logger.isTraceEnabled()) {
                logger.trace(execution.toString());
            }
            execution.shutdown();
            cancellation.shutdown();
        }
    }

    /**
     * Run a set of tasks using a bounded thread pool and awaits completion.
     *
     * @param numThreads max number of threads
     * @param tasks number of tasks
     * @param factoryFn task factory function
     * @param completionFn task completion function
     * @param exceptionFn task exception function
     * @param <V> task type
     */
    public static <V> void runConcurrentlyAndWait(int numThreads,
                                                  int tasks,
                                                  Supplier<Callable<V>> factoryFn,
                                                  Consumer<V> completionFn,
                                                  Function<Throwable, ? extends V> exceptionFn) {
        // Bounded executor
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads / 2, numThreads,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(numThreads));
        executor.setRejectedExecutionHandler((runnable, exec) -> {
            try {
                exec.getQueue().put(runnable);
                if (exec.isShutdown()) {
                    throw new RejectedExecutionException(
                            "Task " + runnable + " rejected from " + exec);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("", e);
            }
        });

        List<CompletableFuture<V>> allFutures = new ArrayList<>();

        IntStream.rangeClosed(1, tasks).forEach(value -> {
            CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            V v = factoryFn.get().call();
                            completionFn.accept(v);
                            return v;
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }, executor)
                    .exceptionallyAsync(exceptionFn);
            allFutures.add(future);
        });

        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[] {}))
                .join();

        executor.shutdown();
    }
}
