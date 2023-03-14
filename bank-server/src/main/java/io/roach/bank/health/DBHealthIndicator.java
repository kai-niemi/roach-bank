package io.roach.bank.health;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DBHealthIndicator extends DataSourceHealthIndicator {
    public DBHealthIndicator(@Autowired DataSource dataSource,
                             @Value("${spring.datasource.hikari.connection-init-sql}") String validationQuery) {
        super(dataSource, validationQuery);
    }
}
