package io.roach.bank.service;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;
import io.roach.bank.config.AccountPlan;
import io.roach.bank.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountPlanBuilder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountPlan accountPlan;

    @Autowired
    private RegionRepository metadataRepository;

    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Implicit
    public void buildAccountPlan() {
        if (accountPlan.isClearAtStartup()) {
            logger.info("Clear existing accounts");
            clearAccounts();
        }

        if (transactionTemplate.execute(status -> metadataRepository.hasAccountPlan())) {
            logger.info("Account plan already exist - skip");
        } else {
            metadataRepository.listCities(metadataRepository.listRegions(List.of()))
                    .parallelStream()
                    .unordered()
                    .forEach(this::createAccounts);
        }
    }

    public void clearAccounts() {
        jdbcTemplate.execute("truncate table transaction_item CASCADE");
        jdbcTemplate.execute("truncate table transaction CASCADE");
        jdbcTemplate.execute("truncate table account CASCADE");
    }

    public void createAccounts(String city) {
        Money balance = Money.of(accountPlan.getInitialBalance(), accountPlan.getCurrency());

        logger.info("Creating %d accounts for city [%s] with initial balance [%s] (total %s)".formatted(
                accountPlan.getAccountsPerCity(),
                city,
                balance,
                balance.multiply(accountPlan.getAccountsPerCity())));

        jdbcTemplate.update(
                "INSERT INTO account (id, city, balance, currency, name, type, closed, allow_negative, updated_at) "
                        + "SELECT gen_random_uuid(),"
                        + " ?,"
                        + " ?,"
                        + " ?,"
                        + " (concat('user:', no::text)),"
                        + " ?::account_type,"
                        + " false,"
                        + " 0,"
                        + " ? "
                        + "FROM generate_series(1, ?) no",
                city,
                balance.getAmount(),
                balance.getCurrency().getCurrencyCode(),
                AccountType.ASSET.getCode(),
                LocalDate.now(),
                accountPlan.getAccountsPerCity());
    }
}
