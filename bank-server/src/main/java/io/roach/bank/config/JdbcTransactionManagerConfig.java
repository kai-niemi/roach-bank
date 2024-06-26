package io.roach.bank.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cockroachdb.aspect.TransactionAttributesAspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.ProfileNames;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_ADVISOR)
@Profile(ProfileNames.JDBC)
public class JdbcTransactionManagerConfig implements TransactionManagementConfigurer {
    @Autowired
    private DataSource dataSource;

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        transactionManager.setGlobalRollbackOnParticipationFailure(false);
        transactionManager.setEnforceReadOnly(true);
        transactionManager.setNestedTransactionAllowed(true);
        transactionManager.setRollbackOnCommitFailure(false);
        transactionManager.setDefaultTimeout(-1);
        return transactionManager;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Profile("!(" + ProfileNames.PSQL_LOCAL + "|" + ProfileNames.PSQL_DEV + ")")
    public TransactionAttributesAspect transactionAttributesAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionAttributesAspect(jdbcTemplate);
    }
}
