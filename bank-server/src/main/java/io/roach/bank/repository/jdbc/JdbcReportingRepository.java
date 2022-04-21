package io.roach.bank.repository.jdbc;

import java.math.BigDecimal;
import java.util.Currency;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.repository.ReportingRepository;

@Repository
@TransactionMandatory
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
    public AccountSummary accountSummary(Currency currency) {
        assertInTransactionContext();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("currency", currency.getCurrencyCode());

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT "
                        + "  count(a.id) tot_accounts, "
                        + "  count(distinct a.city) tot_cities, "
                        + "  sum(a.balance) tot_balance, "
                        + "  min(a.balance) min_balance, "
                        + "  max(a.balance) max_balance "
                        + "FROM account a "
                        + "WHERE a.currency = :currency::currency_code",
                parameters,
                (rs, rowNum) -> {
                    AccountSummary summary = new AccountSummary();
                    summary.setCurrency(currency);
                    summary.setNumberOfAccounts(rs.getInt(1));
                    summary.setNumberOfRegions(rs.getInt(2));
                    summary.setTotalBalance(rs.getBigDecimal(3));
                    summary.setMinBalance(rs.getBigDecimal(4));
                    summary.setMaxBalance(rs.getBigDecimal(5));
                    return summary;
                });
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_TRANSACTION_REPORT_SUMMARY)
    public TransactionSummary transactionSummary(Currency currency) {
        assertInTransactionContext();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("currency", currency.getCurrencyCode());

        // Break down per currency and use parallel queries
        return namedParameterJdbcTemplate.queryForObject(
                "SELECT "
                        + "  count(distinct t.id), "
                        + "  count(ti.transaction_id), "
                        + "  sum(abs(ti.amount)), "
                        + "  sum(ti.amount) "
                        + "FROM transaction t "
                        + "  JOIN transaction_item ti ON t.id=ti.transaction_id "
                        + "WHERE ti.currency = :currency::currency_code",
                parameters,
                (rs, rowNum) -> {
                    TransactionSummary summary = new TransactionSummary();
                    summary.setCurrency(currency);
                    summary.setNumberOfTransactions(rs.getInt(1));
                    summary.setNumberOfLegs(rs.getInt(2));

                    BigDecimal sum = rs.getBigDecimal(3);
                    summary.setTotalTurnover(sum != null ? sum : BigDecimal.ZERO);

                    BigDecimal checksum = rs.getBigDecimal(4);
                    summary.setTotalCheckSum(checksum != null ? checksum : BigDecimal.ZERO);

                    return summary;
                });
    }
}
