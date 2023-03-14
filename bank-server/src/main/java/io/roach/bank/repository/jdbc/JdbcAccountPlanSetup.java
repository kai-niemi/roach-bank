package io.roach.bank.repository.jdbc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;
import io.roach.bank.config.AccountPlan;
import io.roach.bank.repository.MetadataRepository;
import jakarta.annotation.PostConstruct;

@Repository
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JdbcAccountPlanSetup {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountPlan accountPlan;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void init() {
        Set<String> regions = StringUtils.commaDelimitedListToSet(accountPlan.getRegion());

        Set<String> cities = metadataRepository.getRegionCities(regions);

        if (accountPlan.isClearAtStartup()) {
            logger.info("Clear existing account plan");
            clearAccounts();
        }

        if (jdbcTemplate.queryForList("select 1 from account limit 1", Integer.class).isEmpty()) {
            logger.info("Creating new account plan: {}", accountPlan);
            cities.parallelStream().forEach(this::createAccounts);
            logger.info("Creating {} accounts total", (accountPlan.getAccountsPerCity() * cities.size()));
        } else {
            logger.info("Account plan already exist");
        }
    }

    public void clearAccounts() {
        jdbcTemplate.execute("truncate table transaction_item CASCADE");
        jdbcTemplate.execute("truncate table transaction CASCADE");
        jdbcTemplate.execute("truncate table account CASCADE");
    }

    public void createAccounts(String city) {
        Money balance = Money.of(accountPlan.getInitialBalance(), accountPlan.getCurrency());

        logger.info("Creating {} accounts for city '{}' with initial balance {} (total {})",
                accountPlan.getAccountsPerCity(),
                city,
                balance,
                balance.multiply(accountPlan.getAccountsPerCity()));

        jdbcTemplate.update(
                "INSERT INTO account (id, city, balance, currency, name, type, closed, allow_negative, updated) "
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
