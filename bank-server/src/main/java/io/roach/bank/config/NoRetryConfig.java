package io.roach.bank.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import io.cockroachdb.jdbc.spring.aspect.TransactionBoundaryAspect;
import io.roach.bank.ProfileNames;

/**
 * Configuration for normal Spring annotation-driven TX demarcation with
 * REQUIRES_NEW propagation at boundaries (REST controller methods).
 * <p>
 * Does not perform any retrys on the server side but instead propagates
 * serialization errors to the client for retry.
 */
@Configuration
@Profile(ProfileNames.RETRY_NONE)
public class NoRetryConfig {
    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkProfiles() {
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_SAVEPOINT)),
                "Cant have both RETRY_SAVEPOINT and RETRY_NONE");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_DRIVER)),
                "Cant have both RETRY_DRIVER and RETRY_NONE");
    }

    @Bean
    public TransactionBoundaryAspect transactionBoundaryAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionBoundaryAspect(jdbcTemplate);
    }
}

