package io.roach.bank.aspect;

import java.sql.SQLException;
import java.time.Duration;

import org.aspectj.lang.Signature;

public interface RetryHandler {
    boolean isRetryable(SQLException sqlException);

    void handleNonTransientException(SQLException sqlException);

    void handleTransientException(SQLException sqlException,
                                  int methodCalls,
                                  Signature signature,
                                  long maxBackoff);

    void handleTransientExceptionRecovery(SQLException sqlException,
                                          int methodCalls,
                                          Signature signature,
                                          Duration elapsedTime);
}
