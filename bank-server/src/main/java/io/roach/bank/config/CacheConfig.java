package io.roach.bank.config;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {
    public static final String CACHE_ACCOUNT_REPORT_SUMMARY = "accountReportSummary";

    public static final String CACHE_TRANSACTION_REPORT_SUMMARY = "transactionReportSummary";

    @Bean
    @Override
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(CACHE_ACCOUNT_REPORT_SUMMARY),
                new ConcurrentMapCache(CACHE_TRANSACTION_REPORT_SUMMARY))
        );
        return cacheManager;
    }
}
