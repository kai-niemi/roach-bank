package io.roach.bank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.ProfileNames;

@Configuration
@EnableTransactionManagement(order = AdvisorOrder.TRANSACTION_ADVISOR)
@Profile(ProfileNames.JPA)
public class JpaTransactionManagerConfig {
}

