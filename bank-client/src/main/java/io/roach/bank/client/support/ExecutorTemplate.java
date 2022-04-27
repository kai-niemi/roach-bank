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
        logger.info("Started '{}'", id);

        ListenableFuture<Void> future = threadPoolExecutor.submitListenable(() -> {
            final long startTime = System.currentTimeMillis();

            AtomicInteger activeWorkers = workers.computeIfAbsent(id, i -> new AtomicInteger());
            activeWorkers.incrementAndGet();

            CallMetrics.Context context = callMetrics.of(id, activeWorkers::get);

            do {
                final long callTime = context.before();
                try {
                    runnable.run();
                    context.after(callTime, null);
                } catch (Exception e) {
                    context.after(callTime, e);
                    logger.error("Execution error", e);
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
        logger.info("Started '{}'", id);

        ListenableFuture<Void> future = threadPoolExecutor.submitListenable(() -> {
            AtomicInteger activeWorkers = workers.computeIfAbsent(id, i -> new AtomicInteger());
            activeWorkers.incrementAndGet();

            CallMetrics.Context context = callMetrics.of(id, activeWorkers::get);

            for (int i = 0; i < iterations; i++) {
                if (Thread.interrupted() || cancelRequested) {
                    break;
                }

                final long callTime = context.before();
                try {
                    runnable.run();
                    context.after(callTime, null);
                } catch (Exception e) {
                    context.after(callTime, e);
                    logger.error("Execution error", e);
                    break;
                }
            }

            activeWorkers.decrementAndGet();

            logger.info("Finihed '{}'", id);

            return null;
        });
        futures.add(future);
        return future;
    }

    @PreDestroy
    public void shutdown() {
        this.cancelRequested = true;
    }

    public void cancelFutures() {
        cancelRequested = true;
        while (!futures.isEmpty()) {
            logger.info("Cancelling {} futures", futures.size());

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
        logger.info("All futures cancelled");
        cancelRequested = false;
    }

}
