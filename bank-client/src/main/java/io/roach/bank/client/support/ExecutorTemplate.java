package io.roach.bank.client.support;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ExecutorTemplate {
    @FunctionalInterface
    public interface Callback {
        void call(BoundedExecutor boundedExecutor);
    }

    @Autowired
    private BoundedExecutor boundedExecutor;

    @Async
    public void runConcurrently(Callback callback, Duration duration) {
        final long startTime = System.currentTimeMillis();

        do {
            callback.call(boundedExecutor);
        } while (System.currentTimeMillis() - startTime < duration.toMillis()
                && !Thread.interrupted()
                && !boundedExecutor.isShutdown());
    }
}
