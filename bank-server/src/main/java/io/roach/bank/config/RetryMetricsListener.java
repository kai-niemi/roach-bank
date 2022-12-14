package io.roach.bank.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.cockroachdb.jdbc.retry.DefaultRetryListener;
import io.cockroachdb.jdbc.retry.RetryListener;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.roach.bank.ProfileNames;

@Component
@Profile(ProfileNames.RETRY_DRIVER)
public class RetryMetricsListener implements RetryListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final RetryListener loggingListener = new DefaultRetryListener(logger);

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter successful;

    private Counter failed;

    private Counter operations;

    private Counter time;

    @PostConstruct
    public void init() {
        this.successful = meterRegistry.counter("bank.retry.success");
        this.failed = meterRegistry.counter("bank.retry.fail");
        this.operations = meterRegistry.counter("bank.retry.operations");
        this.time = meterRegistry.counter("bank.retry.time");
    }

    @Override
    public void configure(Properties properties) {

    }

    @Override
    public void beforeRetry(Connection connection, int attempt, SQLException ex) {
        loggingListener.beforeRetry(connection, attempt, ex);
    }

    @Override
    public void afterRetryFailure(Connection connection, int attempt, SQLException ex) {
        loggingListener.afterRetryFailure(connection, attempt, ex);

        failed.increment();
    }

    @Override
    public void afterRetrySuccess(Connection connection, int attempt, SQLException ex,
                                  int totalJdbcOperations, long executionTimeMillis) {
        loggingListener.afterRetrySuccess(connection, attempt, ex, totalJdbcOperations, executionTimeMillis);

        successful.increment();
        operations.increment(executionTimeMillis);
        time.increment(executionTimeMillis);
    }
}
