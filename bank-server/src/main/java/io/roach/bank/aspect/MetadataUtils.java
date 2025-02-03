package io.roach.bank.aspect;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

public abstract class MetadataUtils {
    private MetadataUtils() {
    }

    public static boolean isCockroachDB(DataSource dataSource) {
        return databaseVersion(dataSource).contains("CockroachDB");
    }

    public static String databaseVersion(DataSource dataSource) {
        try {
            return new JdbcTemplate(dataSource).queryForObject("select version()", String.class);
        } catch (DataAccessException e) {
            return "unknown";
        }
    }

    public static String databaseIsolation(DataSource dataSource) {
        try {
            return new JdbcTemplate(dataSource)
                    .queryForObject("SHOW transaction_isolation", String.class);
        } catch (DataAccessException e) {
            return "unknown";
        }
    }

    public static boolean hasEnterpriseLicense(DataSource dataSource) {
        try {
            if (isCockroachDB(dataSource)) {
                String license = new JdbcTemplate(dataSource)
                        .queryForObject("SHOW CLUSTER SETTING enterprise.license", String.class);
                return StringUtils.hasLength(license);
            }
            return false;
        } catch (DataAccessException e) {
            return false;
        }
    }
}
