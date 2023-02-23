package io.roach.bank.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.cockroachdb.aspect.TransactionAttributesAspect;
import org.springframework.data.cockroachdb.aspect.TransactionRetryAspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;

/**
 * Transaction management with retries and exponential backoff handled at JDBC Driver level.
 */
@Configuration
@Profile({ProfileNames.RETRY_CLIENT})
public class ClientLevelRetryConfig {
    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkProfiles() {
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_NONE)),
                "Cant have both RETRY_CLIENT and RETRY_NONE");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_SAVEPOINT)),
                "Cant have both RETRY_CLIENT and RETRY_SAVEPOINT");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_DRIVER)),
                "Cant have both RETRY_CLIENT and RETRY_DRIVER");
    }

    @Bean
    @Profile({ProfileNames.CRDB_LOCAL, ProfileNames.CRDB_DEV, ProfileNames.CRDB_CLOUD})
    public TransactionAttributesAspect transactionBoundaryAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionAttributesAspect(jdbcTemplate);
    }

    @Bean
    @Profile({ProfileNames.CRDB_LOCAL, ProfileNames.CRDB_DEV, ProfileNames.CRDB_CLOUD})
    public TransactionRetryAspect transactionRetryAspect() {
        return new TransactionRetryAspect();
    }
}
