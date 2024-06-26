package io.roach.bank;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * Definition of Spring profile names for the application domain.
 */
public abstract class ProfileNames {
    /**
     * Propagate transaction retries to clients on serialization errors.
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
     * Adopt server-side savepoint rollback retries on serialization errors.
     * JDBC only.
     */
    public static final String RETRY_SAVEPOINT = "retry-savepoint";

    /**
     * Use filesystem paths for Thymeleaf templates.
     */
    public static final String DEBUG = "debug";

    public static final String DEMO = "demo";

    public static final String DEFAULT = "default";

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
     * crdb-jdbc driver local.
     */
    public static final String CRDB_LOCAL = "crdb-local";

    /**
     * crdb-jdbc driver dev.
     */
    public static final String CRDB_DEV = "crdb-dev";

    /**
     * pg-jdbc driver dev.
     */
    public static final String PGJDBC_DEV = "pgjdbc-dev";

    /**
     * PostgreSQL local
     */
    public static final String PSQL_LOCAL = "psql-local";

    /**
     * PostgreSQL dev
     */
    public static final String PSQL_DEV = "psql-dev";

    private ProfileNames() {
    }

    public static boolean acceptsPostgresSQL(Environment environment) {
        return environment.acceptsProfiles(Profiles.of(ProfileNames.PSQL_LOCAL, ProfileNames.PSQL_DEV));
    }
}
