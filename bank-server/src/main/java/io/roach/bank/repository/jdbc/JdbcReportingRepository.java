package io.roach.bank.repository.jdbc;

import java.math.BigDecimal;
import java.util.Currency;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.repository.ReportingRepository;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
@Profile(ProfileNames.JDBC)
public class JdbcReportingRepository implements ReportingRepository {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    private void assertInTransactionContext() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX active");
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_ACCOUNT_REPORT_SUMMARY)
    public AccountSummary accountSummary(String city) {
        assertInTransactionContext();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("city", city);

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT "
                            + "  count(a.id) tot_accounts, "
                            + "  sum(a.balance) tot_balance, "
                            + "  min(a.balance) min_balance, "
                            + "  max(a.balance) max_balance, "
                            + "  a.currency "
                            + "FROM account a "
                            + "WHERE a.city = :city "
                            + "GROUP BY a.city, a.currency "
                            + "LIMIT 1", // Assuming single currency
                    parameters,
                    (rs, rowNum) -> {
                        AccountSummary summary = new AccountSummary();
                        summary.setCity(city);
                        summary.setNumberOfAccounts(rs.getInt(1));
                        summary.setTotalBalance(rs.getBigDecimal(2));
                        summary.setMinBalance(rs.getBigDecimal(3));
                        summary.setMaxBalance(rs.getBigDecimal(4));
                        summary.setCurrency(Currency.getInstance(rs.getString(5)));
                        return summary;
                    });
        } catch (EmptyResultDataAccessException e) {
            AccountSummary summary = new AccountSummary();
            summary.setCity(city);
            return summary;
        }
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_TRANSACTION_REPORT_SUMMARY)
    public TransactionSummary transactionSummary(String city) {
        assertInTransactionContext();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("city", city);

        // Break down per currency and use parallel queries
        try {
            return namedParameterJdbcTemplate.queryForObject(
                    "SELECT "
                            + "  count(distinct t.id), "
                            + "  count(ti.transaction_id), "
                            + "  sum(abs(ti.amount)), "
                            + "  sum(ti.amount), "
                            + "  ti.currency "
                            + "FROM transaction t "
                            + "  JOIN transaction_item ti ON t.id=ti.transaction_id "
                            + "WHERE ti.city = :city "
                            + "GROUP BY ti.city, ti.currency "
                            + "LIMIT 1", // Assuming single currency
                    parameters,
                    (rs, rowNum) -> {
                        TransactionSummary summary = new TransactionSummary();
                        summary.setCity(city);
                        summary.setCurrency(Currency.getInstance(rs.getString(5)));
                        summary.setNumberOfTransactions(rs.getInt(1));
                        summary.setNumberOfLegs(rs.getInt(2));

                        BigDecimal sum = rs.getBigDecimal(3);
                        summary.setTotalTurnover(sum != null ? sum : BigDecimal.ZERO);

                        BigDecimal checksum = rs.getBigDecimal(4);
                        summary.setTotalCheckSum(checksum != null ? checksum : BigDecimal.ZERO);

                        return summary;
                    });
        } catch (EmptyResultDataAccessException e) {
            TransactionSummary summary = new TransactionSummary();
            summary.setCity(city);
            return summary;
        }
    }
}
