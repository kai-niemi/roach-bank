package io.roach.bank.client.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
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

public class CallMetrics {
    private static String separator(int len) {
        return new String(new char[len]).replace('\0', '-');
    }

    private static final String HEADER_PATTERN = "%-30s %9s %7s %8s %10s %10s %10s %10s %10s %9s %9s";

    private static final String ROW_PATTERN = "%-30s %,9d %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %,9d %,9d";

    private static final String FOOTER_PATTERN = "%-30s %,9d %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %,9d %,9d";

    public static final int FRAME_SIZE = 200;

    private final SortedMap<String, Context> metrics = Collections.synchronizedSortedMap(new TreeMap<>());

    public Context of(String name, Supplier<Integer> concurrencyCallback) {
        return metrics.computeIfAbsent(name, supplier -> new Context(name, concurrencyCallback));
    }

    public void clear() {
        metrics.clear();
    }

    public void reset() {
        metrics.values().stream().forEach(context -> {
            context.reset();
        });
    }

    public String prettyPrintHeader() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf(Locale.US,
                HEADER_PATTERN,
                "metric",
                "threads",
                "time(s)",
                "op/s",
                "op/m",
                "p50(ms)",
                "p90(ms)",
                "p99(ms)",
                "mean(ms)",
                "ok",
                "fail"
        );
        pw.println();
        pw.printf(Locale.US,
                HEADER_PATTERN,
                separator(30),// metric
                separator(9), // threads
                separator(7), // time
                separator(8), // ops
                separator(10), // opm
                separator(10), // p50
                separator(10), // p90
                separator(10), // p99
                separator(10), // mean
                separator(9), // success
                separator(9) // fail
        );
        pw.println();
        return sw.toString();
    }

    public String prettyPrintBody() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        metrics.forEach((key, value) -> pw.println(value.formatStats()));
        return sw.toString();
    }

    public String prettyPrintFooter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Aggregate
        int concurrencySum = metrics.values().stream().mapToInt(Context::concurrency).sum();

        double timeAvg = metrics.values().stream().mapToDouble(Context::executionTimeSeconds).average()
                .orElse(0);

        double opsPerSecSum = metrics.values().stream().mapToDouble(Context::opsPerSec).sum();
        double opsPerMinSum = metrics.values().stream().mapToDouble(Context::opsPerMin).sum();

        double p50 = metrics.values().stream().mapToDouble(Context::p50).average().orElse(0);
        double p90 = metrics.values().stream().mapToDouble(Context::p90).average().orElse(0);
        double p99 = metrics.values().stream().mapToDouble(Context::p99).average().orElse(0);
        double meanTime = metrics.values().stream().mapToDouble(Context::mean).average().orElse(0);

        int successSum = metrics.values().stream().mapToInt(Context::successfulCalls).sum();
        int failSum = metrics.values().stream().mapToInt(Context::failedCalls).sum();

        pw.printf(Locale.US,
                FOOTER_PATTERN,
                "sum/avg",
                concurrencySum,
                timeAvg,
                opsPerSecSum >= FRAME_SIZE ? '>' : ' ',
                opsPerSecSum,
                opsPerMinSum,
                p50,
                p90,
                p99,
                meanTime,
                successSum,
                failSum
        );

        pw.println();
        pw.flush();

        return sw.toString();
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

        public void reset() {
            successful.set(0);
            failed.set(0);
            snapshots.clear();
        }

        public long before() {
            return System.nanoTime();
        }

        public void after(long beginTime, Throwable t) {
            if (snapshots.size() > FRAME_SIZE) {
                snapshots.remove(0);
            }
            snapshots.add(new Snapshot(beginTime));

            if (t != null) {
                failed.incrementAndGet();
            } else {
                successful.incrementAndGet();
            }
        }

        private double executionTimeSeconds() {
            return Duration.ofNanos(System.nanoTime() - startTime).toMillis() / 1000.0;
        }

        private String formatStats() {
            double p50 = 0;
            double p90 = 0;
            double p99 = 0;

            List<Double> latencies = sortedLatencies();

            if (snapshots.size() > 1) {
                p50 = percentile(latencies, .5);
                p90 = percentile(latencies, .9);
                p99 = percentile(latencies, .99);
            }

            final double opsPerSec = opsPerSec();

            return String.format(Locale.US,
                    ROW_PATTERN,
                    name,
                    concurrencyCallback.get(),
                    executionTimeSeconds(),
                    opsPerSec >= FRAME_SIZE ? '>' : ' ',
                    opsPerSec,
                    opsPerSec * 60,
                    p50,
                    p90,
                    p99,
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

        private double p50() {
            return percentile(sortedLatencies(), .5);
        }

        private double p90() {
            return percentile(sortedLatencies(), .9);
        }

        private double p99() {
            return percentile(sortedLatencies(), .99);
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
            if (latencies.size() > 0) {
                int index = (int) Math.ceil(percentile * latencies.size());
                return latencies.get(index - 1);
            }
            return 0;
        }
    }

    private static class Snapshot implements Comparable<Snapshot> {
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

        @Override
        public int compareTo(Snapshot o) {
            return Long.compare(o.endTime, endTime);
        }
    }
}
