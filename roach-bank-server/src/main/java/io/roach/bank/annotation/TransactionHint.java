package io.roach.bank.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface TransactionHint {
    /**
     * Name of the hint.
     */
    String name();

    /**
     * Value of the hint.
     */
    String value() default "";

    int intValue() default -1;
}
