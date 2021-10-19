package io.roach.bank.aspect;

import org.springframework.core.Ordered;

/**
 * Ordering constants for transaction advisors.
 */
public interface AdvisorOrder {
    int CHANGE_FEED_ADVISOR = Ordered.LOWEST_PRECEDENCE - 1;

    int TX_ATTRIBUTES_ADVISOR = Ordered.LOWEST_PRECEDENCE - 2;

    int TX_ADVISOR = Ordered.LOWEST_PRECEDENCE - 3;

    int TX_RETRY_ADVISOR = Ordered.LOWEST_PRECEDENCE - 4;
}
