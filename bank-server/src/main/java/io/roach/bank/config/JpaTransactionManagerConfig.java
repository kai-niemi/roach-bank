package io.roach.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cockroachdb.aspect.TransactionAttributesAspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.ProfileNames;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_ADVISOR)
@Profile(ProfileNames.JPA)
public class JpaTransactionManagerConfig {
    @Bean
    @Profile({
            ProfileNames.PGJDBC_DEV,
            ProfileNames.CRDB_LOCAL,
            ProfileNames.CRDB_DEV})
    public TransactionAttributesAspect transactionAttributesAspect(JdbcTemplate jdbcTemplate) {
        return new TransactionAttributesAspect(jdbcTemplate);
    }
}

