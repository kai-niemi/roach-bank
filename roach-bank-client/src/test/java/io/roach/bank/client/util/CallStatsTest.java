package io.roach.bank.client.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class CallStatsTest {
    @Test
    public void whenRunningConcurrentTasks_thenPrintLatencyPercentiles_andStuff() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(CallStats::printStdOut, 3, 3, TimeUnit.SECONDS);

        Duration duration = Duration.ofSeconds(30);
        long now = System.currentTimeMillis();
        int tasks = 5;
        int threads = 5;
        int minDelay = 1;
        int maxDelay = 15;

        ExecutorService executorService = Executors.newFixedThreadPool(threads * tasks);

        List<Future<?>> futureList = new ArrayList<>();

        IntStream.rangeClosed(1, tasks).forEach(value -> {
            String name = String.format("task %d", value);

            IntStream.rangeClosed(1, threads).forEach(thread -> {
                Future<?> f = executorService.submit(() -> {
                    CallStats callStats = CallStats.of(name, () -> name, duration);

                    while (System.currentTimeMillis() - now < duration.toMillis()) {
                        long begin = callStats.now();
                        try {
                            long d = (long) (minDelay + Math.random() * (maxDelay - minDelay));
                            Thread.sleep(d);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        double d = (System.nanoTime() - begin) / 1_000_000.0;
                        callStats.mark(begin, null);
                    }

                    return null;
                });
                futureList.add(f);
            });
        });

        while (!futureList.isEmpty()) {
            try {
                futureList.remove(0).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdownNow();

        CallStats.printStdOut();
    }
}
