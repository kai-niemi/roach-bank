package io.roach.bank.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.roach.bank.client.command.support.CallMetrics;

@Configuration
public class AppConfig {
    @Bean
    public CallMetrics callMetrics() {
        return new CallMetrics();
    }
}
