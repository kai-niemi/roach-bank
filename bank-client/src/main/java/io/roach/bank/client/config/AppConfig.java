package io.roach.bank.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.roach.bank.client.command.support.CallMetrics;
import io.roach.bank.client.command.RegionProvider;

@Configuration
public class AppConfig {
    @Bean
    public RegionProvider regionProvider() {
        return new RegionProvider();
    }

    @Bean
    public CallMetrics callMetrics() {
        return new CallMetrics();
    }
}
