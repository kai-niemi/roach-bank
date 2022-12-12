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
import io.cockroachdb.jdbc.spring.aspect.TransactionRetryAspect;
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
    public TransactionRetryAspect transactionRetryAspect() {
        return new TransactionRetryAspect();
    }

    @Bean
    public TransactionBoundaryAspect transactionBoundaryAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionBoundaryAspect(jdbcTemplate);
    }
}
