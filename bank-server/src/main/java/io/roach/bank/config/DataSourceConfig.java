package io.roach.bank.config;

import javax.sql.DataSource;

import org.postgresql.PGProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

import io.cockroachdb.jdbc.CockroachProperty;
import io.roach.bank.ProfileNames;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
public class DataSourceConfig {
    public static final String SQL_TRACE_LOGGER = "io.roach.bank.SQL_TRACE";

    @Autowired
    private Environment environment;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource primaryDataSource() {
        HikariDataSource ds = dataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();

        ds.addDataSourceProperty(PGProperty.REWRITE_BATCHED_INSERTS.getName(), "true");

        if (environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_DRIVER))) {
            ds.addDataSourceProperty(CockroachProperty.IMPLICIT_SELECT_FOR_UPDATE.getName(), "true");
            ds.addDataSourceProperty(CockroachProperty.RETRY_TRANSIENT_ERRORS.getName(), "true");
            ds.addDataSourceProperty(CockroachProperty.RETRY_MAX_ATTEMPTS.getName(), "5");
            ds.addDataSourceProperty(CockroachProperty.RETRY_MAX_BACKOFF_TIME.getName(), "15000");
        }

        return ds;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = primaryDataSource();

        ChainListener listener = new ChainListener();
        return new LazyConnectionDataSourceProxy(ProxyDataSourceBuilder
                .create(dataSource)
                .name("SQL-Trace")
                .listener(listener)
                .asJson()
                .logQueryBySlf4j(SLF4JLogLevel.TRACE, SQL_TRACE_LOGGER)
                .build());
    }
}
