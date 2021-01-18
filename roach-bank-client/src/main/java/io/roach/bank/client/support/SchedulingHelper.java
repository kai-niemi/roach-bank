package io.roach.bank.client.support;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

@Component
public class SchedulingHelper {
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        this.scheduledExecutorService = Executors
                .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @PreDestroy
    public void teardown() {
        try {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelaySec, long periodSec) {
        return scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelaySec, periodSec, TimeUnit.SECONDS);
    }
}
