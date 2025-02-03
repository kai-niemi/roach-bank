package io.roach.bank.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.annotations.Retryable;

/**
 * AOP aspect that automatically retries operations that throw transient SQL exceptions
 * with state code 40001. It applies an around-advice for all annotated methods and
 * intercepts and retries concurrency failures such as deadlock looser,
 * pessimistic and optimistic locking failures.
 * <p>
 * Concurrency related failures are more common for databases running in higher isolation
 * levels such as 1SR when a workload is contended (interleaved RW, WR or WW operations
 * that don't serialize).
 * <p>
 * This aspect is separate from {@link TransactionDecoratorAspect} which allows it to
 * be used directly with {@link org.springframework.transaction.annotation.Transactional}
 * using propagation attribute {@link org.springframework.transaction.annotation.Propagation#REQUIRES_NEW}.
 * The main pre-condition is that no existing transaction can be in scope when attempting
 * a retry, hence it's applicable for transaction boundaries only. The business operation
 * retried must also be idempotent since it can be invoked more than once due to a retry.
 * <p>
 * This advice must be applied before the Spring transaction advisor in the call chain
 * and before the {@link TransactionDecoratorAspect} if used simultaneously.
 * See {@link org.springframework.transaction.annotation.EnableTransactionManagement} for
 * controlling weaving order.
 *
 * @author Kai Niemi
 */
@Aspect
@Order(TransactionRetryAspect.PRECEDENCE)
public class TransactionRetryAspect {
    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_RETRY_ADVISOR;

    public static final String RETRY_ASPECT_CALL_COUNT = "TransactionRetryAspect.retryAttempt";

    static <A extends Annotation> A findAnnotation(ProceedingJoinPoint pjp, Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(pjp.getSignature().getDeclaringType(), annotationType);
    }

    private final RetryHandler retryHandler;

    public TransactionRetryAspect(RetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    @Around(value = "Pointcuts.anyRetryableOperation(retryable)",
            argNames = "pjp,retryable")
    public Object doAroundRetryableOperation(ProceedingJoinPoint pjp, Retryable retryable) throws Throwable {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting no active transaction - check advice @Order and @EnableTransactionManagement order");

        // Grab from type if needed (for non-annotated methods)
        if (retryable == null) {
            retryable = AnnotationUtils.findAnnotation(pjp.getSignature().getDeclaringType(), Retryable.class);
        }

        Assert.notNull(retryable, "No @Retryable annotation found!?");

        int methodCalls = 0;
        SQLException sqlException = null;

        final Instant callTime = Instant.now();

        do {
            final Throwable throwable;
            try {
                methodCalls++;

                TransactionSynchronizationManager.bindResource(RETRY_ASPECT_CALL_COUNT, methodCalls);

                Object rv = pjp.proceed();

                if (methodCalls > 1) {
                    retryHandler.handleTransientExceptionRecovery(sqlException, methodCalls, pjp.getSignature(),
                            Duration.between(callTime, Instant.now()));
                }

                return rv;
            } catch (UndeclaredThrowableException ex) {
                throwable = ex.getUndeclaredThrowable();
            } catch (Exception ex) {
                throwable = ex;
            } finally {
                TransactionSynchronizationManager.unbindResource(RETRY_ASPECT_CALL_COUNT);
            }

            Throwable cause = NestedExceptionUtils.getMostSpecificCause(throwable);
            if (cause instanceof SQLException) {
                sqlException = (SQLException) cause;
                if (retryHandler.isRetryable(sqlException)) {
                    retryHandler.handleTransientException(sqlException, methodCalls, pjp.getSignature(),
                            retryable.maxBackoff());
                } else {
                    retryHandler.handleNonTransientException(sqlException);
                    throw throwable;
                }
            } else {
                throw throwable;
            }
        } while (methodCalls - 1 < retryable.retryAttempts());

        throw new ConcurrencyFailureException(
                "Too many transient SQL errors (" + methodCalls + ") for method ["
                        + pjp.getSignature().toShortString()
                        + "]. Giving up!");
    }
}

