package io.roach.bank.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@TransactionRequiresNew
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
     * @return session/transaction timeout in seconds. Negative value use default timeout.
     */
    int timeout() default -1;

    /**
     * See https://www.cockroachlabs.com/docs/v19.2/set-transaction.html#set-priority
     *
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
