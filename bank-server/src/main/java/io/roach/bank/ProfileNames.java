package io.roach.bank;

/**
 * Definition of Spring profile names for the application domain.
 */
public abstract class ProfileNames {
    public static final String NOT = "!";

    /**
     * Use PostgreSQL database and REPEATABLE_READ (SI) isolation level.
     */
    public static final String DB_POSTGRESQL = "db-psql";

    /**
     * Use CockroachDB database and SERIALIZABLE isolation level.
     */
    public static final String DB_COCKROACH = "db-crdb";

    /**
     * Adopt server-side expontential backoff retrys on serialization errors.
     */
    public static final String RETRY_BACKOFF = "retry-backoff";

    /**
     * Adopt server-side savepoint rollback retrys on serialization errors.
     * JDBC only.
     */
    public static final String RETRY_SAVEPOINT = "retry-savepoint";

    /**
     * Propagate transaction retry's to clients on serialization errors.
     */
    public static final String RETRY_NONE = "retry-none";

    /**
     * Websocket/STOMP events via Kafka listeners via CRDB kafka sink.
     */
    public static final String CDC_KAFKA = "cdc-kafka";

    /**
     * Websocket/STOMP events via REST controller via CRDB http sink.
     */
    public static final String CDC_HTTP = "cdc-http";

    /**
     * Websocket/STOMP events via AOP interceptors (dual-write)
     */
    public static final String CDC_AOP = "cdc-aop";

    /**
     * Enable transactional outbox pattern
     */
    public static final String OUTBOX = "outbox";

    /**
     * Use filesystem paths for Thymeleaf templates.
     */
    public static final String DEV = "dev";

    /**
     * Use CRDB dedicated.
     */
    public static final String CLOUD = "cloud";

    /**
     * Enable JPA repositories over JDBC.
     */
    public static final String JPA = "jpa";

    public static final String NOT_JPA = NOT + "jpa";

    private ProfileNames() {
    }
}
