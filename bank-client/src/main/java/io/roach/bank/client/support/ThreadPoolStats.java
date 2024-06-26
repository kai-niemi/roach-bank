package io.roach.bank.client.support;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ThreadPoolStats {
    public int maximumPoolSize;

    public int poolSize;

    public int activeCount;

    public long corePoolSize;

    public long taskCount;

    public int largestPoolSize;

    public long completedTaskCount;

    public static ThreadPoolStats from(ThreadPoolTaskExecutor boundedExecutor) {
        ThreadPoolExecutor pool = boundedExecutor.getThreadPoolExecutor();
        ThreadPoolStats instance = new ThreadPoolStats();
        instance.poolSize = pool.getPoolSize();
        instance.corePoolSize = pool.getCorePoolSize();
        instance.maximumPoolSize = pool.getMaximumPoolSize();
        instance.activeCount = pool.getActiveCount();
        instance.taskCount = pool.getTaskCount();
        instance.largestPoolSize = pool.getLargestPoolSize();
        instance.completedTaskCount = pool.getCompletedTaskCount();
        return instance;
    }
}
