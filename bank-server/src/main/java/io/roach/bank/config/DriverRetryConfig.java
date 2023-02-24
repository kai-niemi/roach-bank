package io.roach.bank.config;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.cockroachdb.aspect.TransactionAttributesAspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;

@Configuration
@Profile(ProfileNames.RETRY_DRIVER)
public class DriverRetryConfig {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkProfiles() {
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_SAVEPOINT)),
                "Cant have both RETRY_DRIVER and RETRY_SAVEPOINT");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_CLIENT)),
                "Cant have both RETRY_DRIVER and RETRY_CLIENT");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_NONE)),
                "Cant have both RETRY_DRIVER and RETRY_NONE");

        logger.info("Enabled JDBC-driver level retrys");
    }

    @Bean
    public TransactionAttributesAspect transactionAttributesAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionAttributesAspect(jdbcTemplate);
    }
}
