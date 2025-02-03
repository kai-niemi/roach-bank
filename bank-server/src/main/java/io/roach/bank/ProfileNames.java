package io.roach.bank;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * Definition of Spring profile names for the application domain.
 */
public abstract class ProfileNames {
    public static final String DEFAULT = "default";

    /**
     * Use filesystem paths for Thymeleaf templates.
     */
    public static final String DEBUG = "debug";

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

    public static final String PSQL = "psql";

    public static final String CRDB = "!psql";

    private ProfileNames() {
    }

    public static boolean acceptsPostgresSQL(Environment environment) {
        return environment.acceptsProfiles(Profiles.of(ProfileNames.PSQL));
    }
}
