package io.roach.bank.client.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolStats {
    public static ThreadPoolStats from(BoundedExecutor boundedExecutor) {
        ThreadPoolStats instance = new ThreadPoolStats();
        ExecutorService executorService = boundedExecutor.getExecutorService();
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
            instance.corePoolSize = pool.getCorePoolSize();
            instance.poolSize = pool.getPoolSize();
            instance.maximumPoolSize = pool.getMaximumPoolSize();
            instance.activeCount = pool.getActiveCount();
            instance.taskCount = pool.getTaskCount();
            instance.largestPoolSize = pool.getLargestPoolSize();
            instance.completedTaskCount = pool.getCompletedTaskCount();
        }
        return instance;
    }

    public int maximumPoolSize;

    public int poolSize;

    public int activeCount;

    public long corePoolSize;

    public long taskCount;

    public int largestPoolSize;

    public long completedTaskCount;
}
