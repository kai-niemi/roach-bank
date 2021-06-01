package io.roach.bank.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.aspect.AdvisorOrder;

/**
 * Configuration for normal Spring annotation-driven TX demarcation with
 * REQUIRES_NEW propagation at boundaries (REST controller methods).
 * <p>
 * Does not perform any retrys on the server side but instead propagates
 * serialization errors to the client for retry.
 */
@Configuration
@Profile(ProfileNames.RETRY_NONE)
@EnableTransactionManagement(order = AdvisorOrder.TX_ADVISOR)
public class DefaultTransactionManagement implements TransactionManagementConfigurer {
    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void checkProfiles() {
        for (String profile : env.getActiveProfiles()) {
            Assert.isTrue(!ProfileNames.RETRY_BACKOFF.equals(profile), "Conflicting spring profiles");
            Assert.isTrue(!ProfileNames.RETRY_SAVEPOINT.equals(profile), "Conflicting spring profiles");
        }
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}

