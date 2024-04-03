package io.roach.bank.service;

import io.roach.bank.AccountPlan;
import io.roach.bank.ApplicationModel;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.Region;
import io.roach.bank.api.support.Money;
import io.roach.bank.repository.RegionRepository;
import io.roach.bank.util.AsciiArt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountPlanBuilder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationModel applicationModel;

    @Autowired
    private RegionRepository regionRepository;

    public void buildAccountPlan() {
        logger.info("Building account plan '%s'".formatted(applicationModel.getName()));

        if (applicationModel.isClearAtStartup()) {
            logger.info("Clear existing account plan");
            clearAccounts();
        }


        if (regionRepository.hasExistingAccountPlan()) {
            logger.info("Account plan already exist - skip creating");
        } else {
            logger.info("Creating account plan");

            applicationModel.getRegions().forEach(region -> regionRepository.createRegion(region));

            regionRepository.createRegionMappings(applicationModel.getRegionMapping());

            List<Region> regions = regionRepository.listRegions(List.of());

            regionRepository.listCities(regions)
                    .parallelStream()
                    .unordered()
                    .forEach(this::createAccounts);
        }

        logger.info("Bank is now open for business %s".formatted(AsciiArt.shrug()));
    }

    public void clearAccounts() {
        jdbcTemplate.execute("truncate table transaction_item CASCADE");
        jdbcTemplate.execute("truncate table transaction CASCADE");
        jdbcTemplate.execute("truncate table account CASCADE");
        jdbcTemplate.execute("truncate table region_mapping CASCADE");
        jdbcTemplate.execute("truncate table region CASCADE");
    }

    public Money createAccounts(String city) {
        AccountPlan accountPlan = applicationModel.getAccountPlan();

        Money balance = Money.of(accountPlan.getInitialBalance(), accountPlan.getCurrency());

        logger.info("Creating %,d accounts for city [%s] with initial balance [%s] and total of [%s]".formatted(
                accountPlan.getAccountsPerCity(),
                city,
                balance,
                balance.multiply(accountPlan.getAccountsPerCity())));

        jdbcTemplate.update(
                "INSERT INTO account (city, balance, currency, name, type, closed, allow_negative, updated_at) "
                        + "SELECT "
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

        return balance.multiply(accountPlan.getAccountsPerCity());
    }
}
