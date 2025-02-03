package io.roach.bank.aspect;

import org.springframework.core.Ordered;

/**
 * Ordering constants for transaction advisors.
 */
public interface AdvisorOrder {
    /**
     * Retry advice should have top priority, before any transaction is created.
     */
    int TRANSACTION_RETRY_ADVISOR = Ordered.LOWEST_PRECEDENCE - 5;

    /**
     * Transaction manager advice.
     */
    int TRANSACTION_MANAGER_ADVISOR = Ordered.LOWEST_PRECEDENCE - 4;

    /**
     * Transaction session attribute advice.
     */
    int TRANSACTION_ATTRIBUTES_ADVISOR = Ordered.LOWEST_PRECEDENCE - 3;

    /**
     * Any post business transaction advice, potentially within a transaction scope.
     */
    int CHANGE_FEED_ADVISOR = Ordered.LOWEST_PRECEDENCE - 2;
}
