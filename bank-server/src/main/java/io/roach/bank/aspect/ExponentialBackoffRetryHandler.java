package io.roach.bank.aspect;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExponentialBackoffRetryHandler implements RetryHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean isRetryable(SQLException sqlException) {
        // 40001 is the only state code we are looking for in terms of safe retries
//        return PSQLState.SERIALIZATION_FAILURE.getState().equals(sqlException.getSQLState());
        return "40001".equals(sqlException.getSQLState());
    }

    @Override
    public void handleNonTransientException(SQLException sqlException) {
        sqlException.forEach(ex -> {
            SQLException nested = (SQLException) ex;
            logger.warn("Non-transient SQL error (%s): %s".formatted(
                    nested.getSQLState(), nested.getMessage()));
        });
    }

    @Override
    public void handleTransientException(SQLException sqlException, int methodCalls, Signature signature,
                                         long maxBackoff) {
        try {
            long backoffMillis = Math.min((long) (Math.pow(2, methodCalls)
                    + ThreadLocalRandom.current().nextInt(1000)), maxBackoff);

            logger.warn("Transient SQL error (%s) for method [%s] attempt (%d) backoff %s ms: %s".formatted(
                    sqlException.getSQLState(),
                    signature.toShortString(),
                    methodCalls,
                    backoffMillis,
                    sqlException.getMessage()));

            TimeUnit.MILLISECONDS.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void handleTransientExceptionRecovery(SQLException sqlException, int methodCalls, Signature signature,
                                                 Duration elapsedTime) {
        logger.info("Recovered from transient SQL error (%s) for method [%s] attempt (%d) time spent: %s"
                .formatted(sqlException.getSQLState(),
                        signature.toShortString(),
                        methodCalls,
                        elapsedTime.toString()));
    }
}
