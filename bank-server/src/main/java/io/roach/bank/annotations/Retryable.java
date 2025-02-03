package io.roach.bank.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for business service methods that utilize a transient SQL exception retry strategy.
 * A method that is annotated as {@code @Retryable} will automatically be candidate for re-invocation on
 * concurrency failures such as deadlock looser, optimistic locking failures, etc.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Retryable {
    /**
     * @return number of times to retry aborted transient data access exceptions with
     * exponential backoff. Zero or negative value disables retries.
     */
    int retryAttempts() default 10;

    /**
     * @return max backoff time in millis
     */
    long maxBackoff() default 15000;
}
