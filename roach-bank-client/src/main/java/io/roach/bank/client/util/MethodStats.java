package io.roach.bank.client.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MethodStats {
    private static final String HEADER_PATTERN = "%-30s %9s %7s %8s %10s %10s %10s %10s %9s %9s %9s";

    private static final String ROW_PATTERN = "%-30s %8.1f%% %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %9d %9d %9d";

    private static class Snapshot implements Comparable<Snapshot> {
        final long creationTime;

        final long methodTime;

        public Snapshot(long methodTime) {
            this.creationTime = System.nanoTime();
            this.methodTime = methodTime;
        }

        public long durationNanos() {
            return Math.abs(creationTime - methodTime);
        }

        @Override
        public int compareTo(Snapshot o) {
            return Long.compare(o.creationTime, creationTime);
        }
    }

    private static final ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();

    public static final int FRAME_SIZE = 50000;

    public static MethodStats of(String key, Supplier<String> displayName) {
        return of(key, displayName, Duration.ZERO);
    }

    public static MethodStats of(String key, Supplier<String> displayName, Duration duration) {
        return methodStats.computeIfAbsent(key, k -> new MethodStats(displayName, duration));
    }

    public static boolean remove(String key) {
        return methodStats.remove(key) != null;
    }

    public static void printStdOut() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            printStats(pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print(sw.toString());
    }

    private static void printStats(PrintWriter pw) {
        if (methodStats.isEmpty()) {
            return;
        }

        pw.printf(Locale.US, HEADER_PATTERN,
                "name",
                "progress",
                "time(s)",
                "ops/s",
                "ops/m",
                "p50(ms)",
                "p90(ms)",
                "p99(ms)",
                "ops",
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
                separator(10), // p95
                separator(9), // calls
                separator(9), // success
                separator(9) // fail
        );
        pw.println();

        methodStats.forEach((key, value) -> pw.println(value.formatStats()));

        // Aggregate

        int keys = methodStats.size();
        double avgProgress = methodStats.values().stream().mapToDouble(MethodStats::executionProgress).average()
                .orElse(0);
        double avgTime = methodStats.values().stream().mapToDouble(MethodStats::executionTimeSeconds).average()
                .orElse(0);
        double opsPerSec = methodStats.values().stream().mapToDouble(MethodStats::opPerSec).sum();
        double opsPerMin = methodStats.values().stream().mapToDouble(MethodStats::opPerMin).sum();
        double p50 = methodStats.values().stream().mapToDouble(MethodStats::p50).average().orElse(0);
        double p90 = methodStats.values().stream().mapToDouble(MethodStats::p90).average().orElse(0);
        double p99 = methodStats.values().stream().mapToDouble(MethodStats::p99).average().orElse(0);
        int callCount = methodStats.values().stream().mapToInt(MethodStats::callsTotal).sum();
        int callSuccessful = methodStats.values().stream().mapToInt(MethodStats::callsSuccess).sum();
        int callFailed = methodStats.values().stream().mapToInt(MethodStats::callsFail).sum();

        pw.printf(Locale.US,
                "%-30s %8.1f%% %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %9d %9d %9d",
                "SUM (" + keys + ")",
                avgProgress,
                avgTime,
                opsPerSec >= FRAME_SIZE ? '>' : ' ',
                opsPerSec,
                opsPerMin,
                p50,
                p90,
                p99,
                callCount,
                callSuccessful,
                callFailed
        );

        pw.println();
        pw.flush();
    }

    private static String separator(int len) {
        return new String(new char[len]).replace('\0', '-');
    }

    private final Supplier<String> name;

    private final Duration duration;

    private final long startTime;

    private final AtomicInteger callCount = new AtomicInteger();

    private final AtomicInteger callSuccessful = new AtomicInteger();

    private final AtomicInteger callFailed = new AtomicInteger();

    private final AtomicLong tickTime = new AtomicLong();

    private final List<Snapshot> windowFrames = Collections.synchronizedList(new LinkedList<>());

    private MethodStats(Supplier<String> name, Duration duration) {
        this.name = name;
        this.duration = duration;
        this.startTime = System.nanoTime();
    }

    public void beforeMethod() {
        tickTime.set(System.nanoTime());
    }

    public void afterMethod(Throwable t) {
        if (windowFrames.size() > FRAME_SIZE) {
            windowFrames.remove(0);
        }
        windowFrames.add(new Snapshot(tickTime.get()));

        callCount.incrementAndGet();
        if (t != null) {
            callFailed.incrementAndGet();
        } else {
            callSuccessful.incrementAndGet();
        }
    }

    private double executionTimeSeconds() {
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis() / 1000.0;
    }

    private double executionProgress() {
        return Math.min(1.0, executionTimeSeconds() / (double) duration.getSeconds()) * 100.0;
    }

    public String formatStats() {
        double p50 = 0;
        double p90 = 0;
        double p99 = 0;
        double opsPerSec = opPerSec();

        if (windowFrames.size() > 1) {
            p50 = percentile(.5);
            p90 = percentile(.9);
            p99 = percentile(.99);
        }

        return String.format(Locale.US,
                ROW_PATTERN,
                name.get(),
                executionProgress(),
                executionTimeSeconds(),
                opsPerSec >= FRAME_SIZE ? '>' : ' ',
                opsPerSec,
                opsPerSec * 60,
                p50,
                p90,
                p99,
                callCount.get(),
                callSuccessful.get(),
                callFailed.get()
        );
    }

    private double opPerSec() {
        final int size = windowFrames.size();

        return size / Math.max(1,
                Duration.ofNanos(
                        (System.nanoTime() - (windowFrames.isEmpty() ? 0 : windowFrames.get(0).creationTime)))
                        .toMillis() / 1000.0);
    }

    private double opPerMin() {
        return opPerSec() * 60;
    }

    private double p50() {
        return percentile(.5);
    }

    private double p90() {
        return percentile(.9);
    }

    private double p99() {
        return percentile(.99);
    }

    private int callsTotal() {
        return callCount.get();
    }

    private int callsSuccess() {
        return callSuccessful.get();
    }

    private int callsFail() {
        return callFailed.get();
    }

    private double percentile(double p) {
        final int size = windowFrames.size();

        List<Snapshot> copy = Arrays.asList(windowFrames.toArray(new Snapshot[] {}));
        copy.sort(Comparator.comparingLong(Snapshot::durationNanos));

        return copy.parallelStream().collect(Collectors.toList())
                .subList(0, (int) (size * p))
                .stream().map(Snapshot::durationNanos).collect(Collectors.toList())
                .stream().max(Long::compare).orElse(0L) / 1000000.0;
    }
}
