package io.roach.bank.util;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

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

}
