package io.roach.bank.client.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.roach.bank.client.support.BoundedExecutor;

@Configuration
@EnableAsync
public class ConcurrencyConfig implements AsyncConfigurer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(250);
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Bean(destroyMethod = "shutdown")
    public BoundedExecutor boundedExecutor() {
        return new BoundedExecutor(corePoolSize());
    }

    private int corePoolSize() {
        return Runtime.getRuntime().availableProcessors() * 4;
    }
}
