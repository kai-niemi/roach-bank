package io.roach.bank.client.command.support;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.boot.ansi.AnsiColor;

public class CallMetrics {
    private static String separator(int len) {
        return new String(new char[len]).replace('\0', '-');
    }

    private static final String HEADER_PATTERN = "%-35s %9s %7s %8s %10s %10s %10s %10s %10s %9s %9s";

    private static final String ROW_PATTERN = "%-35s %,9d %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %,9d %,9d";

    private static final String FOOTER_PATTERN = "%-35s %,9d %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %,9d %,9d";

    private final SortedMap<String, Context> metrics = Collections.synchronizedSortedMap(new TreeMap<>());

    public Context of(String name, Supplier<Integer> concurrencyCallback) {
        return metrics.computeIfAbsent(name, supplier -> new Context(name, concurrencyCallback));
    }

    public void clear() {
        metrics.clear();
    }

    public void prettyPrint(Console console) {
        console.text(AnsiColor.BRIGHT_WHITE,
                HEADER_PATTERN,
                "metric",
                "threads",
                "time(s)",
                "op/s",
                "op/m",
                "p90",
                "p99",
                "p999",
                "mean",
                "ok",
                "fail"
        );
        console.text(AnsiColor.BRIGHT_WHITE,
                HEADER_PATTERN,
                separator(35),// metric
                separator(9), // threads
                separator(7), // time
                separator(8), // ops
                separator(10), // opm
                separator(10), // p90
                separator(10), // p99
                separator(10), // p999
                separator(10), // mean
                separator(9), // success
                separator(9) // fail
        );

        metrics.forEach((key, value) -> console.text(AnsiColor.BRIGHT_GREEN, value.printStats()));

        int concurrencySum = metrics.values().stream().mapToInt(Context::concurrency).sum();
        double timeAvg = metrics.values().stream().mapToDouble(Context::executionTimeSeconds).average()
                .orElse(0);
        double opsPerSecSum = metrics.values().stream().mapToDouble(Context::opsPerSec).sum();
        double opsPerMinSum = metrics.values().stream().mapToDouble(Context::opsPerMin).sum();
        double p90 = metrics.values().stream().mapToDouble(Context::p90).average().orElse(0);
        double p99 = metrics.values().stream().mapToDouble(Context::p99).average().orElse(0);
        double p999 = metrics.values().stream().mapToDouble(Context::p999).average().orElse(0);
        double meanTime = metrics.values().stream().mapToDouble(Context::mean).average().orElse(0);
        int successSum = metrics.values().stream().mapToInt(Context::successfulCalls).sum();
        int failSum = metrics.values().stream().mapToInt(Context::failedCalls).sum();

        console.text(AnsiColor.BRIGHT_YELLOW,
                FOOTER_PATTERN,
                "sum/avg",
                concurrencySum,
                timeAvg,
                ' ',
                opsPerSecSum,
                opsPerMinSum,
                p90,
                p99,
                p999,
                meanTime,
                successSum,
                failSum
        );
    }

    public static class Context {
        private final String name;

        private final Supplier<Integer> concurrencyCallback;

        private final long startTime = System.nanoTime();

        private final AtomicInteger successful = new AtomicInteger();

        private final AtomicInteger failed = new AtomicInteger();

        private final List<Snapshot> snapshots = Collections.synchronizedList(new LinkedList<>());

        private Context(String name, Supplier<Integer> concurrencyCallback) {
            this.name = name;
            this.concurrencyCallback = concurrencyCallback;
        }

        public long before() {
            return System.nanoTime();
        }

        public void after(long beginTime, Throwable t) {
            evictTail();

            // Purge snapshots older than 1min
            snapshots.add(new Snapshot(beginTime));

            if (t != null) {
                failed.incrementAndGet();
            } else {
                successful.incrementAndGet();
            }
        }

        private void evictTail() {
            final Instant bound = Instant.now().minusSeconds(60);
            snapshots.removeIf(snapshot -> snapshot.getMark().isBefore(bound));
        }

        private double executionTimeSeconds() {
            return Duration.ofNanos(System.nanoTime() - startTime).toMillis() / 1000.0;
        }

        private String printStats() {
            evictTail();

            List<Double> latencies = sortedLatencies();

            double p90 = 0;
            double p99 = 0;
            double p999 = 0;

            if (snapshots.size() > 1) {
                p90 = percentile(latencies, .9);
                p99 = percentile(latencies, .99);
                p999 = percentile(latencies, .999);
            }

            final double opsPerSec = opsPerSec();

            return String.format(Locale.US,
                    ROW_PATTERN,
                    name,
                    concurrencyCallback.get(),
                    executionTimeSeconds(),
                    ' ',
                    opsPerSec,
                    opsPerSec * 60,
                    p90,
                    p99,
                    p999,
                    mean(),
                    successful.get(),
                    failed.get()
            );
        }

        private double opsPerSec() {
            final int size = snapshots.size();
            return size / Math.max(1,
                    Duration.ofNanos(
                                    (System.nanoTime() - (snapshots.isEmpty() ? 0 : snapshots.get(0).endTime)))
                            .toMillis() / 1000.0);
        }

        private double opsPerMin() {
            return opsPerSec() * 60;
        }

        private double p90() {
            return percentile(sortedLatencies(), .9);
        }

        private double p99() {
            return percentile(sortedLatencies(), .99);
        }

        private double p999() {
            return percentile(sortedLatencies(), .999);
        }

        private int successfulCalls() {
            return successful.get();
        }

        private int failedCalls() {
            return failed.get();
        }

        private int concurrency() {
            return concurrencyCallback.get();
        }

        private double mean() {
            List<Snapshot> copy = new ArrayList<>(snapshots);
            return copy.stream().mapToDouble(Snapshot::durationMillis)
                    .average()
                    .orElse(0);
        }

        private List<Double> sortedLatencies() {
            List<Snapshot> copy = new ArrayList<>(snapshots);
            return copy.stream().map(Snapshot::durationMillis)
                    .sorted()
                    .collect(Collectors.toList());
        }

        private double percentile(List<Double> latencies, double percentile) {
            if (percentile < 0 || percentile > 1) {
                throw new IllegalArgumentException(">=0 N <=1");
            }
            if (!latencies.isEmpty()) {
                int index = (int) Math.ceil(percentile * latencies.size());
                return latencies.get(index - 1);
            }
            return 0;
        }
    }

    private static class Snapshot implements Comparable<Snapshot> {
        final Instant mark = Instant.now();

        final long endTime = System.nanoTime();

        final long beginTime;

        Snapshot(long beginTime) {
            if (beginTime > endTime) {
                throw new IllegalArgumentException();
            }
            this.beginTime = beginTime;
        }

        public double durationMillis() {
            return (endTime - beginTime) / 1_000_000.0;
        }

        public Instant getMark() {
            return mark;
        }

        @Override
        public int compareTo(Snapshot o) {
            return Long.compare(o.endTime, endTime);
        }
    }
}
