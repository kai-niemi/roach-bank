package io.roach.bank.client.support;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
public class ExecutorTemplate {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, AtomicInteger> workers = new ConcurrentHashMap<>();

    private final LinkedList<ListenableFuture<Void>> futures = new LinkedList<>();

    private volatile boolean cancelRequested;

    @Qualifier("jobExecutor")
    @Autowired
    private ThreadPoolTaskExecutor threadPoolExecutor;

    @Autowired
    private CallMetrics callMetrics;

    public ListenableFuture<Void> runAsync(String id, Runnable runnable, Duration duration) {
        logger.info("Started '{}' to run for {}", id, duration);

        ListenableFuture<Void> future = threadPoolExecutor.submitListenable(() -> {
            final long startTime = System.currentTimeMillis();

            AtomicInteger activeWorkers = workers.computeIfAbsent(id, i -> new AtomicInteger());
            activeWorkers.incrementAndGet();

            CallMetrics.Context context = callMetrics.of(id, activeWorkers::get);

            int fails = 0;
            do {
                final long callTime = context.before();
                try {
                    runnable.run();
                    context.after(callTime, null);
                } catch (HttpClientErrorException e) {
                    context.after(callTime, e);
                    logger.warn("HTTP 4xx error - backing off", e);
                    backoff(++fails);
                } catch (HttpServerErrorException e) {
                    context.after(callTime, e);
                    logger.warn("HTTP 5xx error - backing off", e);
                    backoff(++fails);
                } catch (Exception e) {
                    context.after(callTime, e);
                    logger.error("Uncategorized error - cancelling prematurely", e);
                    break;
                }
            } while (System.currentTimeMillis() - startTime < duration.toMillis()
                    && !Thread.interrupted() && !cancelRequested);

            activeWorkers.decrementAndGet();

            logger.info("Finihed '{}'", id);

            return null;
        });
        futures.add(future);
        return future;
    }

    public ListenableFuture<Void> runAsync(String id, Runnable runnable, int iterations) {
        logger.info("Started '{}' to run {} times", id, iterations);

        ListenableFuture<Void> future = threadPoolExecutor.submitListenable(() -> {
            AtomicInteger activeWorkers = workers.computeIfAbsent(id, i -> new AtomicInteger());
            activeWorkers.incrementAndGet();

            CallMetrics.Context context = callMetrics.of(id, activeWorkers::get);

            loop:
            for (int i = 0; i < iterations; i++) {
                if (Thread.interrupted() || cancelRequested) {
                    break;
                }

                for (int fails=0; fails<10; fails++) {
                    final long callTime = context.before();
                    try {
                        runnable.run();
                        context.after(callTime, null);
                    } catch (HttpClientErrorException e) {
                        context.after(callTime, e);
                        logger.warn("HTTP 4xx error - backing off", e);
                        backoff(++fails);
                    } catch (HttpServerErrorException e) {
                        context.after(callTime, e);
                        logger.error("HTTP 5xx error - cancelling", e);
                        break loop;
                    } catch (Exception e) {
                        context.after(callTime, e);
                        logger.error("Uncategorized error - cancelling", e);
                        break loop;
                    }
                }
            }

            activeWorkers.decrementAndGet();

            logger.info("Finihed '{}'", id);

            return null;
        });
        futures.add(future);
        return future;
    }

    private void backoff(int fails) {
        try {
            long backoffMillis = Math.min((long) (Math.pow(2, ++fails) + Math.random() * 1000), 5000);
            Thread.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void shutdown() {
        this.cancelRequested = true;
    }

    public void cancelFutures() {
        cancelRequested = true;
        while (!futures.isEmpty()) {
            logger.debug("Cancelling {} futures", futures.size());

            ListenableFuture<Void> future = futures.pop();
            future.cancel(true);
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (CancellationException e) {
                //
            } catch (ExecutionException e) {
                logger.error("", e.getCause());
            }
        }
        logger.debug("All futures cancelled");
        cancelRequested = false;
    }
}
