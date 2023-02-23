package io.roach.bank.repository.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
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

        Set<String> cities;
        if (regions.isEmpty()) {
            cities = new HashSet<>();
            metadataRepository.getAllRegionCities().values().forEach(cities::addAll);
        } else {
            cities = metadataRepository.getRegionCities(regions);
        }

        if (accountPlan.isClearAtStartup()) {
            logger.info("Clear existing account plan");
            clearAccounts();
        }

        int n = Objects.requireNonNull(
                jdbcTemplate.queryForObject("select count(1) from account limit 1", Integer.class));
        if (n == 0) {
            logger.info("Creating account plan: {}", accountPlan);
            cities.parallelStream().forEach(this::createAccounts);
            logger.info("Creating {} accounts total", (accountPlan.getNumAccountsPerCity() * cities.size()));
        } else {
            logger.info("Account plan already exist");
        }
    }

    public void clearAccounts() {
        if (accountPlan.isClearTransitive()) {
            jdbcTemplate.execute("truncate table transaction_item CASCADE");
            jdbcTemplate.execute("truncate table transaction CASCADE");
        }
        jdbcTemplate.execute("truncate table account" + (accountPlan.isClearTransitive() ? " CASCADE" : ""));
    }

    public void createAccounts(String city) {
        Money balance = Money.of(accountPlan.getInitialBalance(), accountPlan.getCurrency());

        logger.info("Creating {} accounts for {} with balance {} (total {})",
                accountPlan.getNumAccountsPerCity(), city, balance,
                balance.multiply(accountPlan.getNumAccountsPerCity()));

        jdbcTemplate.update(
                "INSERT INTO account (id, city, balance, currency, name, type, closed, allow_negative, updated) "
                        + "SELECT gen_random_uuid(),?,?,?,?,?,false,0,? "
                        + "FROM generate_series(1, ?)",
                city,
                balance.getAmount(),
                balance.getCurrency().getCurrencyCode(),
                "user:" + nextNameSequence(),
                AccountType.ASSET.getCode(),
                LocalDate.now(),
                accountPlan.getNumAccountsPerCity());
    }

    private final List<Integer> sequenceBatch = new ArrayList<>();

    private int nextNameSequence() {
        if (sequenceBatch.isEmpty()) {
            int nextNum = Objects.requireNonNull(
                    jdbcTemplate.queryForObject("select nextval('account_name_sequence')", Integer.class));
            IntStream.rangeClosed(1, 64).forEach(value -> {
                sequenceBatch.add(nextNum + value);
            });
        }
        return sequenceBatch.remove(0);
    }
}
