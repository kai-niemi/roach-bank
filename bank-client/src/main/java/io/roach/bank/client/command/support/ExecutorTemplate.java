package io.roach.bank.client.command.support;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import jakarta.annotation.PreDestroy;

@Component
public class ExecutorTemplate {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, AtomicInteger> workers = new ConcurrentHashMap<>();

    private final LinkedList<Future<Void>> futures = new LinkedList<>();

    private volatile boolean cancelRequested;

    @Qualifier("workloadExecutor")
    @Autowired
    private ThreadPoolTaskExecutor threadPoolExecutor;

    @Autowired
    private CallMetrics callMetrics;

    public Future<Void> runAsync(String id, Runnable runnable, Duration duration) {
        Future<Void> future = threadPoolExecutor.submitListenable(() -> {
            final long startTime = System.currentTimeMillis();

            AtomicInteger activeWorkers = workers.computeIfAbsent(id, i -> new AtomicInteger());
            activeWorkers.incrementAndGet();

            CallMetrics.Context context = callMetrics.of(id, activeWorkers::get);

            int fails = 0;
            do {
                if (Thread.interrupted() || cancelRequested) {
                    logger.warn("Cancelled '{}'", id);
                    break;
                }

                final long callTime = context.before();
                try {
                    runnable.run();
                    context.after(callTime, null);
                } catch (RestClientException e) {
                    context.after(callTime, e);
                    backoffDelay(++fails, e);
                } catch (Exception e) {
                    context.after(callTime, e);
                    logger.error("Uncategorized error - cancelling prematurely", e);
                    break;
                }
            } while (System.currentTimeMillis() - startTime < duration.toMillis());

            activeWorkers.decrementAndGet();

            logger.info("Finihed '{}'", id);

            return null;
        });
        futures.add(future);
        logger.info("Started '{}' to run for {}", id, duration);
        return future;
    }

    public Future<Void> runAsync(String id, Runnable runnable, int iterations) {
        Future<Void> future = threadPoolExecutor.submitListenable(() -> {
            AtomicInteger activeWorkers = workers.computeIfAbsent(id, i -> new AtomicInteger());
            activeWorkers.incrementAndGet();

            CallMetrics.Context context = callMetrics.of(id, activeWorkers::get);

            outerLoop:
            for (int i = 0; i < iterations; i++) {
                if (Thread.interrupted() || cancelRequested) {
                    logger.warn("Cancelled '{}'", id);
                    break;
                }

                for (int fails = 0; ; fails++) { // Repeat indefinately
                    final long callTime = context.before();
                    try {
                        runnable.run();
                        context.after(callTime, null);
                        break;
                    } catch (RestClientException e) {
                        context.after(callTime, e);
                        backoffDelay(++fails, e);
                    } catch (Exception e) {
                        context.after(callTime, e);
                        logger.error("Uncategorized error - cancelling prematurely", e);
                        break outerLoop;
                    }
                }
            }

            activeWorkers.decrementAndGet();

            logger.info("Finihed '{}'", id);

            return null;
        });
        futures.add(future);
        logger.info("Started '{}' to run {} times", id, iterations);
        return future;
    }

    private void backoffDelay(int fails, RestClientException ex) {
        try {
            long backoffMillis = Math.min((long) (Math.pow(2, fails) + Math.random() * 1000), 10_000);
            if (ex instanceof HttpStatusCodeException) {
                HttpStatusCode code = ((HttpStatusCodeException) ex).getStatusCode();
                if (code.is4xxClientError()) {
                    logger.warn("HTTP client error {} - backing off {} ms due to: {}",
                            code,
                            backoffMillis,
                            ex.getMessage());
                } else if (code.is5xxServerError()) {
                    logger.warn("HTTP server error {} - backing off {} ms due to: {}",
                            code,
                            backoffMillis,
                            ex.getMessage());
                } else {
                    logger.warn("HTTP uncategorized error {} - backing off {} ms due to: {}",
                            code,
                            backoffMillis,
                            ex.getMessage());
                }
            } else {
                logger.warn("Communication error - backing off {} ms due to: {}",
                        backoffMillis,
                        ex.getMessage());
            }
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

            Future<Void> future = futures.pop();
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