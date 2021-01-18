package io.roach.bank.config.data;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.aspect.AdvisorOrder;
import io.roach.bank.aspect.RetryableTransactionalAspect;

/**
 * Transaction management with retries and exponential backoff. Enables conventional Spring
 * annotation-driven TX demarcation, preferably with REQUIRES_NEW propagation at service boundaries
 * (REST controller methods).
 * <p>
 * Puts a retry advice before each transactional joinpoint. If a concurrency based serialization
 * error occurs that translates to a TransientException, then the entire atomic transaction is
 * re-executed, following an exponential backoff strategy.
 */
@Configuration
@Profile({ProfileNames.RETRY_BACKOFF})
@EnableTransactionManagement(order = AdvisorOrder.TX_ADVISOR)
public class BackoffTransactionManagement {
    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void checkProfiles() {
        for (String profile : env.getActiveProfiles()) {
            Assert.isTrue(!ProfileNames.RETRY_NONE.equals(profile), "Conflicting spring profiles");
            Assert.isTrue(!ProfileNames.RETRY_SAVEPOINT.equals(profile), "Conflicting spring profiles");
        }
    }

    @Bean
    public RetryableTransactionalAspect retryableTransactionalAspect() {
        return new RetryableTransactionalAspect();
    }
}
