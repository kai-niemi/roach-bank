package io.roach.bank.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for submitting concurrent workers with a collective timeout and
 * graceful cancellation.
 */
public abstract class TimeBoundExecution {
    private static final Logger logger = LoggerFactory.getLogger(TimeBoundExecution.class);

    private TimeBoundExecution() {
    }

    static String millisecondsToDisplayString(long timeMillis) {
        double seconds = (timeMillis / 1000.0) % 60;
        int minutes = (int) ((timeMillis / 60000) % 60);
        int hours = (int) ((timeMillis / 3600000));

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format("%dh", hours));
        }
        if (hours > 0 || minutes > 0) {
            sb.append(String.format("%dm", minutes));
        }
        if (hours == 0 && seconds > 0) {
            sb.append(String.format(Locale.US, "%.1fs", seconds));
        }
        return sb.toString();
    }

    static <V> long executionTime(Callable<V> task) {
        try {
            long start = System.nanoTime();
            task.call();
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
            logger.debug("{} completed in {}", task, millisecondsToDisplayString(millis));
            return millis;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
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

    public static <V> void runConcurrently(List<Callable<V>> tasks, long timeout, TimeUnit timeUnit) {
        runConcurrently(tasks, timeout, timeUnit, null);
    }

    public static <V> void runConcurrently(List<Callable<V>> tasks, long timeout, TimeUnit timeUnit,
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
                    logger.warn("Task interrupt: " + e.toString());
                } catch (CancellationException e) {
                    logger.warn("Task cancellation: " + e.toString());
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
}
