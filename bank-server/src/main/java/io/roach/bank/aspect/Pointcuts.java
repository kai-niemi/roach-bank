package io.roach.bank.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.annotation.TransactionHints;

/**
 * Shared AOP pointcut expression used across services and components.
 */
@Aspect
public class Pointcuts {
    @Pointcut("within(io.roach.bank..*) "
            + "&& @within(transactionBoundary) || @annotation(transactionBoundary)")
    public void anyTransactionBoundaryOperation(TransactionBoundary transactionBoundary) {
    }

    @Pointcut("within(io.roach.bank..*) "
            + "&& @annotation(transactionHints)")
    public void anyTransactionHintedOperation(TransactionHints transactionHints) {
    }
}
