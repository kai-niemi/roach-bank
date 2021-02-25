package io.roach.bank.client.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CallStats {
    private static final String HEADER_PATTERN = "%-30s %9s %7s %8s %10s %10s %10s %10s %10s %9s %9s";

    private static final String ROW_PATTERN = "%-30s %8.1f%% %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %9d %9d";

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

    private static final ConcurrentHashMap<String, CallStats> all = new ConcurrentHashMap<>();

    public static final int FRAME_SIZE = 50000;

    public static CallStats of(String key, Supplier<String> displayName) {
        return of(key, displayName, TimeDuration.of(Duration.ZERO));
    }

    public static CallStats of(String key, Supplier<String> displayName, TaskDuration duration) {
        return all.computeIfAbsent(key, k -> new CallStats(displayName, duration));
    }

    public static void remove(String key) {
        all.remove(key);
    }

    public static void printStdOut() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        print(pw);
        System.out.print(sw.toString());
    }

    public static void print(PrintWriter pw) {
        if (all.isEmpty()) {
            return;
        }

        pw.printf(Locale.US,
                HEADER_PATTERN,
                "name",
                "progress",
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
        pw.printf(Locale.US, HEADER_PATTERN,
                separator(30),// name
                separator(9), // progress
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

        all.forEach((key, value) -> pw.println(value.formatStats()));

        // Aggregate

        double avgProgress = all.values().stream().mapToDouble(CallStats::executionProgress).average()
                .orElse(0);
        double avgTime = all.values().stream().mapToDouble(CallStats::executionTimeSeconds).average()
                .orElse(0);

        double opsPerSec = all.values().stream().mapToDouble(CallStats::opPerSec).sum();
        double opsPerMin = all.values().stream().mapToDouble(CallStats::opPerMin).sum();

        double p50 = all.values().stream().mapToDouble(CallStats::p50).average().orElse(0);
        double p90 = all.values().stream().mapToDouble(CallStats::p90).average().orElse(0);
        double p99 = all.values().stream().mapToDouble(CallStats::p99).average().orElse(0);
        double mean = all.values().stream().mapToDouble(CallStats::mean).average().orElse(0);
        long samples = all.values().stream().mapToInt(CallStats::callsSuccess).count();

        int callSuccessful = all.values().stream().mapToInt(CallStats::callsSuccess).sum();
        int callFailed = all.values().stream().mapToInt(CallStats::callsFail).sum();

        pw.printf(Locale.US,
                "%-30s %8.1f%% %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %9d %9d",
                "SUM (" + samples + ")",
                avgProgress,
                avgTime,
                opsPerSec >= FRAME_SIZE ? '>' : ' ',
                opsPerSec,
                opsPerMin,
                p50,
                p90,
                p99,
                mean,
                callSuccessful,
                callFailed
        );

        pw.println();
        pw.flush();
    }

    private static String separator(int len) {
        return new String(new char[len]).replace('\0', '-');
    }

    private final Supplier<String> labelCallback;

    private final TaskDuration duration;

    private final AtomicInteger callCount = new AtomicInteger();

    private final AtomicInteger callSuccessful = new AtomicInteger();

    private final AtomicInteger callFailed = new AtomicInteger();

    private final List<Snapshot> frames = Collections.synchronizedList(new LinkedList<>());

    private CallStats(Supplier<String> labelCallback, TaskDuration duration) {
        this.labelCallback = labelCallback;
        this.duration = duration;
    }

    public long now() {
        return System.nanoTime();
    }

    public void mark(long beginTime, Throwable t) {
        if (frames.size() > FRAME_SIZE) {
            frames.remove(0);
        }
        frames.add(new Snapshot(beginTime));

        callCount.incrementAndGet();
        if (t != null) {
            callFailed.incrementAndGet();
        } else {
            callSuccessful.incrementAndGet();
        }
    }

    private double executionTimeSeconds() {
        return duration.executionTimeSeconds();
    }

    private double executionProgress() {
        return duration.executionProgress();
    }

    private String formatStats() {
        double p50 = 0;
        double p90 = 0;
        double p99 = 0;

        if (frames.size() > 1) {
            List<Double> latencies = latenciesSorted();
            p50 = percentile(latencies, .5);
            p90 = percentile(latencies, .9);
            p99 = percentile(latencies, .99);
        }

        double opsPerSec = opPerSec();

        return String.format(Locale.US,
                ROW_PATTERN,
                labelCallback.get(),
                executionProgress(),
                executionTimeSeconds(),
                opsPerSec >= FRAME_SIZE ? '>' : ' ',
                opsPerSec,
                opsPerSec * 60,
                p50,
                p90,
                p99,
                mean(),
                callSuccessful.get(),
                callFailed.get()
        );
    }

    private double opPerSec() {
        final int size = frames.size();
        return size / Math.max(1,
                Duration.ofNanos(
                        (System.nanoTime() - (frames.isEmpty() ? 0 : frames.get(0).endTime)))
                        .toMillis() / 1000.0);
    }

    private double opPerMin() {
        return opPerSec() * 60;
    }

    private double p50() {
        return percentile(latenciesSorted(), .5);
    }

    private double p90() {
        return percentile(latenciesSorted(), .9);
    }

    private double p99() {
        return percentile(latenciesSorted(), .99);
    }

    private int callsSuccess() {
        return callSuccessful.get();
    }

    private int callsFail() {
        return callFailed.get();
    }

    private double mean() {
        List<Snapshot> copy = new ArrayList<>(frames);
        return copy.stream().mapToDouble(Snapshot::durationMillis)
                .average()
                .orElse(0);
    }

    private List<Double> latenciesSorted() {
        List<Snapshot> copy = new ArrayList<>(frames);
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
