package io.roach.bank.client.support;

public interface TaskDuration {
    boolean progress();

    double executionTimeSeconds();

    double executionProgress();
}
