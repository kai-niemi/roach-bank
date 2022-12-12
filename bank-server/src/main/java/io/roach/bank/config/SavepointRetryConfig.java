package io.roach.bank.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import io.cockroachdb.jdbc.spring.aspect.SavepointRetryAspect;
import io.cockroachdb.jdbc.spring.aspect.TransactionBoundaryAspect;
import io.roach.bank.ProfileNames;

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

    @Profile({ProfileNames.CRDB_LOCAL, ProfileNames.CRDB_ODIN, ProfileNames.CRDB_SLEIPNER})
    @Bean
    public SavepointRetryAspect savepointTransactionAspect(PlatformTransactionManager transactionManager) {
        return new SavepointRetryAspect(transactionManager, "cockroach_restart");
    }

    @Profile({ProfileNames.PSQL_LOCAL, ProfileNames.PSQL_SLEIPNER})
    @Bean
    public SavepointRetryAspect savepointTransactionAspectUnnamed(
            PlatformTransactionManager transactionManager) {
        return new SavepointRetryAspect(transactionManager, null);
    }

    @Bean
    public TransactionBoundaryAspect transactionBoundaryAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionBoundaryAspect(jdbcTemplate);
    }
}
