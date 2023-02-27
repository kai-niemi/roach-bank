package io.roach.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.cockroachdb.aspect.SavepointRetryAspect;
import org.springframework.data.cockroachdb.aspect.TransactionAttributesAspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import jakarta.annotation.PostConstruct;

/**
 * Transaction management configuration that puts a savepoint
 * retry advice before each transactional joinpoint. If a database
 * serialization error occurs that translates to a TransientException,
 * then the transaction is repeatingly rolled back to the savepoint.
 * <p>
 * See https://www.cockroachlabs.com/docs/transactions.html#transaction-retries
 */
@Configuration
@Profile(ProfileNames.RETRY_SAVEPOINT)
public class SavepointRetryConfig {
    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkProfiles() {
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.JPA)),
                "Savepoints are not supported in JPA/Hibernate");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_NONE)),
                "Cant have both RETRY_SAVEPOINT and RETRY_NONE");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_DRIVER)),
                "Cant have both RETRY_SAVEPOINT and RETRY_DRIVER");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_CLIENT)),
                "Cant have both RETRY_SAVEPOINT and RETRY_CLIENT");
    }

    @Bean
    public SavepointRetryAspect savepointTransactionAspect(PlatformTransactionManager transactionManager) {
        return new SavepointRetryAspect(transactionManager, "cockroach_restart");
    }
}
