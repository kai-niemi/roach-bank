package io.roach.bank.annotation;

public enum TimeTravelMode {
    /**
     * Non-authoritative reads from closest range replicas.
     * https://www.cockroachlabs.com/docs/v20.1/follower-reads.html
     */
    FOLLOWER_READ,
    /**
     * Non-authoritative reads from a relative timestamp.
     * https://www.cockroachlabs.com/docs/v20.1/as-of-system-time
     */
    SNAPSHOT_READ,
    /**
     * Authoritative reads (default in CockroachDB)
     */
    NONE
}
