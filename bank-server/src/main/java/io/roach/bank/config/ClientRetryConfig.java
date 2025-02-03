package io.roach.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.roach.bank.aspect.ExponentialBackoffRetryHandler;
import io.roach.bank.aspect.RetryHandler;
import io.roach.bank.aspect.TransactionRetryAspect;

/**
 * Transaction management with retries and exponential backoff handled at JDBC Driver level.
 */
@Configuration
public class ClientRetryConfig {
    @Bean
    public TransactionRetryAspect transactionRetryAspect() {
        return new TransactionRetryAspect(retryHandler());
    }

    @Bean
    public RetryHandler retryHandler() {
        return new ExponentialBackoffRetryHandler();
    }
}
