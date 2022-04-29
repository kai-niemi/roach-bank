package io.roach.bank.repository;

import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

@Service
public class AccountPlanBuilder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    public void createAccountPlan() {
        Map<String, Object> plan = readAccountPlan();

        if (plan.isEmpty()) {
            logger.info("Account plan not found or already created");
            return;
        }

        logger.info("Creating account plan: {}", plan);

        Currency currency = Currency.getInstance((String) plan.getOrDefault("currency", "USD"));
        String balance = (String) plan.getOrDefault("balance", "500000.00");
        String prefix = (String) plan.getOrDefault("name_prefix", "user:");
        int accountsPerRegion = (int) (long) plan.getOrDefault("accounts_per_region", 10000L);
        int batchSize = 32;
        AtomicInteger sequence = new AtomicInteger();

        metadataRepository.getRegionCities().forEach((region, cities) -> {
            final Money initialBalance = Money.of(balance, currency).divide(cities.size());

            final int accountsPerCity = accountsPerRegion / cities.size();

            logger.info("Creating {} accounts in region '{}' with cities '{}'",
                    accountsPerRegion, region, cities);

            cities.forEach(city -> {
                final long startTime = System.currentTimeMillis();

                accountRepository.createAccounts(() -> Account.builder()
                                .withName(String.format("%s%05d", prefix, sequence.incrementAndGet()))
                                .withDescription(CockroachFacts.nextFact(256))
                                .withCity(city)
                                .withBalance(initialBalance)
                                .withAccountType(AccountType.ASSET)
                                .build(),
                        accountsPerCity, batchSize);

                logger.info("Created {} accounts in '{}' with total balance {} in {} ms",
                        accountsPerCity,
                        city,
                        initialBalance.multiply(accountsPerCity),
                        System.currentTimeMillis() - startTime);
            });
        });

        markPopulated();
    }

    private void markPopulated() {
        jdbcTemplate.update("update account_plan set initialized= true");
    }

    private Map<String, Object> readAccountPlan() {
        try {
            return jdbcTemplate.queryForMap("select * from account_plan where initialized = false");
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyMap();
        }
    }
}
