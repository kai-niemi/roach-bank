package io.roach.bank.client.support;

import java.time.Duration;

public class TimeDuration implements TaskDuration {
    public static TimeDuration of(Duration duration) {
        return new TimeDuration(duration);
    }

    public static TimeDuration ofSeconds(int seconds) {
        return new TimeDuration(Duration.ofSeconds(seconds));
    }

    private final long startTime = System.nanoTime();

    private final Duration duration;

    protected TimeDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public boolean progress() {
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis() < duration.toMillis();
    }

    @Override
    public double executionTimeSeconds() {
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis() / 1000.0;
    }

    @Override
    public double executionProgress() {
        return Math.min(1.0, executionTimeSeconds() / (double) duration.getSeconds()) * 100.0;
    }

    @Override
    public String toString() {
        return "TimeDuration{" +
                "duration=" + duration +
                '}';
    }
}
