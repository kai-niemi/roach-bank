package io.roach.bank.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.ProfileNames;
import io.roach.bank.aspect.TransactionDecoratorAspect;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_ADVISOR)
@Profile(ProfileNames.JPA)
public class JpaTransactionManagerConfig {
    @Bean
    @Profile({ProfileNames.CRDB})
    public TransactionDecoratorAspect transactionAttributesAspect(DataSource dataSource) {
        return new TransactionDecoratorAspect(dataSource);
    }
}

