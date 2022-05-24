package io.roach.bank.client.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.roach.bank.client.support.CallMetrics;

@Configuration
@EnableAsync
public class ConcurrencyConfig implements AsyncConfigurer {
    @Override
    @Bean(name = "jobExecutor")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setQueueCapacity(Runtime.getRuntime().availableProcessors() * 4);
        executor.setMaxPoolSize(300);
        executor.setWaitForTasksToCompleteOnShutdown(true);
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

    @Bean
    public CallMetrics callMetrics() {
        return new CallMetrics();
    }
}
