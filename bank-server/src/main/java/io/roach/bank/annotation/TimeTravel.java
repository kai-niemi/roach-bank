package io.roach.bank.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that the annotated class or method can read from a
 * given timestamp in the past. Follower reads in CockroachDB
 * represents a computed time interval sufficiently in the past
 * for reads to be served by closest follower replica.
 */
@Inherited
@Documented
@Target({})
@Retention(RUNTIME)
public @interface TimeTravel {
    /**
     * @return the time travel mode, either follower read or snapshot
     */
    TimeTravelMode mode() default TimeTravelMode.NONE;

    /**
     * @return use time bounded or exact staleness (default) on follower reads
     */
    String followerReadStaleness() default "(exact)";

    /**
     * See https://www.cockroachlabs.com/docs/stable/interval.html
     *
     * @return interval expression (ignored if FOLLOWER_READ mode is used)
     */
    String interval() default "-30s";
}
