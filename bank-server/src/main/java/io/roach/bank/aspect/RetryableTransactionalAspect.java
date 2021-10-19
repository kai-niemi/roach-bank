package io.roach.bank.aspect;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.TransactionSystemException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.roach.bank.annotation.TransactionBoundary;

/**
 * AOP around advice that intercepts and retries transient concurrency exceptions such
 * as deadlock looser, pessmistic and optimistic locking failures. Methods matching
 * the pointcut expression (annotated with @TransactionBoundary) are retried a number
 * of times with exponential backoff.
 * <p>
 * NOTE: This advice needs to runs in a non-transactional context, that is before the
 * underlying transaction advisor.
 */
@Aspect
@Order(AdvisorOrder.TX_RETRY_ADVISOR) // This advisor must be before the TX advisor in the call chain
public class RetryableTransactionalAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter successCounter;

    private Counter abortCounter;

    private Counter retryCounter;

    @PostConstruct
    public void init() {
        successCounter = meterRegistry.counter("bank.txn.success");
        abortCounter = meterRegistry.counter("bank.txn.abort");
        retryCounter = meterRegistry.counter("bank.txn.retry");
    }

    @Around(value = "io.roach.bank.aspect.Pointcuts.anyTransactionBoundaryOperation(transactionBoundary)",
            argNames = "pjp,transactionBoundary")
    public Object doInTransaction(ProceedingJoinPoint pjp, TransactionBoundary transactionBoundary)
            throws Throwable {
        // Grab from type if needed (for non-annotated methods)
        if (transactionBoundary == null) {
            transactionBoundary = AopSupport.findAnnotation(pjp, TransactionBoundary.class);
        }

        int numCalls = 0;

        final Instant callTime = Instant.now();

        do {
            try {
                numCalls++;
                Object rv = pjp.proceed();
                successCounter.increment();
                if (numCalls > 1) {
                    logger.debug(
                            "Transient error recovered after " + numCalls + " of " + transactionBoundary
                                    .retryAttempts() + " retries ("
                                    + Duration.between(callTime, Instant.now()).toString() + ")");
                }
                return rv;
            } catch (TransientDataAccessException | TransactionSystemException ex) { // TX abort on commit's
                Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
                if (cause instanceof SQLException) {
                    SQLException sqlException = (SQLException) cause;
                    meterRegistry.counter("bank.txt.error." + sqlException.getSQLState()).increment();
                    if ("40001".equals(sqlException.getSQLState())) { // Transient error code
                        handleTransientException(sqlException, numCalls, pjp.getSignature().toShortString(),
                                transactionBoundary.maxBackoff());
                        continue;
                    }
                }

                abortCounter.increment();
                throw ex;
            } catch (UndeclaredThrowableException ex) {
                Throwable t = ex.getUndeclaredThrowable();
                while (t instanceof UndeclaredThrowableException) {
                    t = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
                }

                Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
                if (cause instanceof SQLException) {
                    SQLException sqlException = (SQLException) cause;
                    meterRegistry.counter("bank.txt.error." + sqlException.getSQLState()).increment();
                    if ("40001".equals(sqlException.getSQLState())) { // Transient error code
                        handleTransientException(sqlException, numCalls, pjp.getSignature().toShortString(),
                                transactionBoundary.maxBackoff());
                        continue;
                    }
                }

                abortCounter.increment();
                throw ex;
            }
        } while (numCalls < transactionBoundary.retryAttempts());

        throw new ConcurrencyFailureException("Too many transient errors (" + numCalls + ") for method ["
                + pjp.getSignature().toShortString() + "]. Giving up!");
    }

    private void handleTransientException(SQLException ex, int numCalls, String method, long maxBackoff) {
        retryCounter.increment();

        try {
            long backoffMillis = Math.min((long) (Math.pow(2, numCalls) + Math.random() * 1000), maxBackoff);
            if (numCalls <= 1 && logger.isWarnEnabled()) {
                logger.warn("Transient error (backoff {}ms) in call {} to '{}': {}",
                        backoffMillis, numCalls, method, ex.getMessage());
            }
            Thread.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
