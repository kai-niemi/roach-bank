package io.roach.bank;

import org.springframework.core.Ordered;

/**
 * Ordering constants for transaction advisors.
 */
public interface AdvisorOrder {
    int CHANGE_FEED_ADVISOR = Ordered.LOWEST_PRECEDENCE - 1;

    int TRANSACTION_ADVISOR = Ordered.LOWEST_PRECEDENCE - 3;
}
