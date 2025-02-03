package io.roach.bank.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicates the annotated class or method is a transactional service boundary. Its architectural role is to
 * delegate to control services or repositories to perform actual business logic processing in
 * the context of a new transaction.
 * <p/>
 * Marks the annotated class as {@link org.springframework.transaction.annotation.Transactional @Transactional}
 * with propagation level {@link org.springframework.transaction.annotation.Propagation#REQUIRES_NEW REQUIRES_NEW},
 * clearly indicating that a new transaction is started before method entry.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Transactional(propagation = Propagation.REQUIRES_NEW)
public @interface TransactionExplicit {
    /**
     * (Optional) Indicates that the annotated class or method can read from a
     * given timestamp in the past. Follower reads in CockroachDB
     * represents a computed time interval sufficiently in the past
     * for reads to be served by closest follower replica.
     */
    TimeTravel timeTravel() default @TimeTravel(mode = TimeTravelMode.DISABLED);

    /**
     * Sets the 'transaction_read_only' session variable.
     */
    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;

    /**
     * Sets the 'idle_in_transaction_session_timeout' session variable.
     */
    String idleTimeout() default "0s";

    /**
     * Always sets the transaction priority, if not set to the default (normal).
     */
    TransactionPriority priority() default TransactionPriority.NORMAL;

    /**
     * Sets the transaction priority on a retry attempt, if not set to the default (normal).
     * Overrides the {@link #priority()}() attribute.
     * Only applies when using the @{@link TransactionExplicit} meta-annotation.
     */
    TransactionPriority retryPriority() default TransactionPriority.NORMAL;

    /**
     * Sets the 'application_name' session variable.
     */
    String applicationName() default "(empty)";

    /**
     * Optional collection of arbitrary session and local variables.
     */
//    SetVariable[] variables() default {};
}
