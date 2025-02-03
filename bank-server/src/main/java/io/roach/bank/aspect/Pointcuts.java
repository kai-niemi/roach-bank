package io.roach.bank.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import io.roach.bank.annotations.Retryable;
import io.roach.bank.annotations.TransactionExplicit;


@Aspect
public class Pointcuts {
    /**
     * Pointcut expression matching all transactional boundaries.
     */
    @Pointcut("execution(public * *(..)) "
              + "&& @annotation(transactionExplicit)")
    public void anyTransactionBoundaryOperation(TransactionExplicit transactionExplicit) {
    }

    /**
     * Pointcut expression matching all retryable operations.
     */
    @Pointcut("execution(public * *(..)) "
              + "&& @annotation(retryable)")
    public void anyRetryableOperation(Retryable retryable) {
    }
}

