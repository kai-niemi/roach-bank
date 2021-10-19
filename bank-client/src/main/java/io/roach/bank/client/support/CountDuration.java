package io.roach.bank.client.support;

import java.time.Duration;

public class CountDuration implements TaskDuration {
    public static CountDuration of(int count) {
        return new CountDuration(count);
    }

    private final long startTime = System.nanoTime();

    private final int targetCount;

    private int actualCount;

    protected CountDuration(int targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public boolean progress() {
        return ++actualCount <= targetCount;
    }

    @Override
    public double executionTimeSeconds() {
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis() / 1000.0;
    }

    @Override
    public double executionProgress() {
        return Math.min(1.0, actualCount / (double) Math.max(1, targetCount)) * 100.0;
    }

    @Override
    public String toString() {
        return "CountDuration{" +
                "targetCount=" + targetCount +
                '}';
    }
}
