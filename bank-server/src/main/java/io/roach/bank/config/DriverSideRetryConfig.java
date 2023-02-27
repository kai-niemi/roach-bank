package io.roach.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import jakarta.annotation.PostConstruct;

@Configuration
@Profile(ProfileNames.RETRY_DRIVER)
public class DriverSideRetryConfig {
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
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.PSQL_LOCAL, ProfileNames.PSQL_DEV)),
                "Cant have driver-level retries for PSQL");

        logger.info("Enabled JDBC-driver level retrys");
    }
}
