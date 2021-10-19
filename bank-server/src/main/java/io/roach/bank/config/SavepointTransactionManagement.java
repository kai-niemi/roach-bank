package io.roach.bank.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.aspect.AdvisorOrder;
import io.roach.bank.aspect.SavepointTransactionalAspect;

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
@EnableTransactionManagement(order = AdvisorOrder.TX_ADVISOR)
public class SavepointTransactionManagement {
    @Autowired
    private ConfigurableEnvironment env;

    @PostConstruct
    public void checkProfiles() {
        for (String profile : env.getActiveProfiles()) {
            Assert.isTrue(!ProfileNames.RETRY_BACKOFF.equals(profile), "Conflicting spring profiles");
            Assert.isTrue(!ProfileNames.RETRY_NONE.equals(profile), "Conflicting spring profiles");
            Assert.isTrue(!ProfileNames.JPA.equals(profile), "Savepoints are not supported in JPA/Hibernate");
        }
    }

    @Profile(ProfileNames.DB_COCKROACH)
    @Bean
    public SavepointTransactionalAspect savepointTransactionAspect() {
        return new SavepointTransactionalAspect("cockroach_restart", TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    @Profile(ProfileNames.DB_POSTGRESQL)
    @Bean
    public SavepointTransactionalAspect savepointTransactionAspectUnnamed() {
        return new SavepointTransactionalAspect(null, TransactionDefinition.ISOLATION_SERIALIZABLE);
    }
}
