package io.roach.bank;

/**
 * Definition of Spring profile names for the application domain.
 */
public abstract class ProfileNames {
    /**
     * Adopt server-side expontential backoff retrys on serialization errors.
     */
    public static final String RETRY_DEFAULT = "retry-default";

    /**
     * Propagate transaction retry's to clients on serialization errors.
     */
    public static final String RETRY_NONE = "retry-none";

    /**
     * Adopt server-side savepoint rollback retrys on serialization errors.
     * JDBC only.
     */
    public static final String RETRY_SAVEPOINT = "retry-savepoint";

    /**
     * Websocket/STOMP events via AOP interceptors (dual-write)
     */
    public static final String CDC_DEFAULT = "cdc-default";

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
     * CockroachDB local.
     */
    public static final String CRDB_LOCAL = "crdb-local";

    /**
     * CockroachDB dedicated cluster.
     */
    public static final String CRDB_ODIN = "crdb-odin";

    /**
     * CockroachDB local self-hosted.
     */
    public static final String CRDB_SLEIPNER = "crdb-sleipner";

    /**
     *
     * PSQL local self-hosted.
     */
    public static final String PSQL_LOCAL = "psql-local";

    /**
     *
     * PSQL local self-hosted.
     */
    public static final String PSQL_SLEIPNER = "psql-sleipner";

    private ProfileNames() {
    }
}
