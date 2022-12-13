package io.roach.bank.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import io.cockroachdb.jdbc.retry.RetryListener;
import io.roach.bank.util.SpringApplicationContext;

public class RetryListenerDelegate implements RetryListener {
    private RetryListener delegate;

    @Override
    public void configure(Properties properties) {
        this.delegate = SpringApplicationContext.getBean(RetryListener.class);
    }

    @Override
    public void beforeRetry(Connection connection, int attempt, SQLException ex) {
        delegate.beforeRetry(connection, attempt, ex);
    }

    @Override
    public void afterRetryFailure(Connection connection, int attempt, SQLException ex) {
        delegate.afterRetryFailure(connection, attempt, ex);
    }

    @Override
    public void afterRetrySuccess(Connection connection, int attempt, SQLException ex,
                                  int totalJdbcOperations, long executionTimeMillis) {
        delegate.afterRetrySuccess(connection, attempt, ex, totalJdbcOperations,
                executionTimeMillis);
    }
}
