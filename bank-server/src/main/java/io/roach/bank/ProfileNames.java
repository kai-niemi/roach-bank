package io.roach.bank;

/**
 * Definition of Spring profile names for the application domain.
 */
public abstract class ProfileNames {
    /**
     * Propagate transaction retry's to clients on serialization errors.
     */
    public static final String RETRY_NONE = "retry-none";

    /**
     * Handle serialization errors at JDBC drive-level
     */
    public static final String RETRY_DRIVER = "retry-driver";

    /**
     * Handle serialization errors at app/client level
     */
    public static final String RETRY_CLIENT = "retry-client";

    /**
     * Adopt server-side savepoint rollback retrys on serialization errors.
     * JDBC only.
     */
    public static final String RETRY_SAVEPOINT = "retry-savepoint";

    /**
     * Websocket/STOMP events via AOP interceptors (dual-write)
     */
    public static final String CDC_NONE = "cdc-none";

    /**
     * Websocket/STOMP events via Kafka listeners via CRDB kafka sink.
     */
    public static final String CDC_KAFKA = "cdc-kafka";

    /**
     * Websocket/STOMP events via REST controller via CRDB webhook sink.
     */
    public static final String CDC_HTTP = "cdc-http";

    /**
     * Use filesystem paths for Thymeleaf templates.
     */
    public static final String DEV = "dev";

    /**
     * Enable transactional outbox pattern.
     */
    public static final String OUTBOX = "outbox";

    /**
     * Enable JPA repositories over JDBC.
     */
    public static final String JPA = "jpa";

    /**
     * Enable JDBC repositories.
     */
    public static final String JDBC = "!jpa";

    /**
     * crdb-jdbc local.
     */
    public static final String CRDB_LOCAL = "crdb-local";

    /**
     * crdb-jdbc dev.
     */
    public static final String CRDB_DEV = "crdb-dev";

    /**
     * crdb-jdbc dedicated/serverless.
     */
    public static final String CRDB_CLOUD = "crdb-cloud";

    /**
     * pgjdbc driver local
     */
    public static final String PSQL_LOCAL = "psql-local";

    /**
     * pgjdbc driver dev
     */
    public static final String PSQL_DEV = "psql-dev";

    private ProfileNames() {
    }
}
