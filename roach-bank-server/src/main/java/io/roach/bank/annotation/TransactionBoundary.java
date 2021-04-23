package io.roach.bank.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicates the annotated class or method is a transactional service boundary. It's architectural role is to
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
public @interface TransactionBoundary {
    /**
     * (Optional) Indicates that the annotated class or method can read from a
     * given timestamp in the past. Follower reads in CockroachDB
     * represents a computed time interval sufficiently in the past
     * for reads to be served by closest follower replica.
     */
    TimeTravel timeTravel() default @TimeTravel();

    /**
     * @return number of times to retry aborted transient data access exceptions with exponential backoff (up to 5s per cycle). Zero or negative value disables retries.
     */
    int retryAttempts() default 10;

    /**
     * @return max backoff time in millis
     */
    long maxBackoff() default 30000;

    /**
     * @return transaction read-only hint optimization
     */
    boolean readOnly() default false;

    /**
     * The amount of time a statement can run before being stopped.
     * https://www.cockroachlabs.com/docs/v20.2/set-vars
     *
     * @return statement timeout in seconds. Negative value use default timeout.
     */
    int statementTimeout() default -1;

    /**
     * Automatically terminates sessions that idle past the specified threshold.
     * @return When set to 0, the session will not timeout. Negative value use default timeout.
     */
    int idleInSessionTimeout() default -1;

    /**
     * Automatically terminates sessions that are idle in a transaction past the specified threshold.
     *
     * @return When set to 0, the session will not timeout. Negative value use default timeout.
     */
    int idleInTransactionSessionTimeout() default -1;

    /**
     * @return sets the transaction priority
     */
    Priority priority() default Priority.normal;

    Vectorize vectorize() default Vectorize.auto;

    String applicationName() default "(default)";

    enum Priority {
        normal,
        low,
        high
    }

    enum Vectorize {
        auto,
        on,
        off
    }
}
