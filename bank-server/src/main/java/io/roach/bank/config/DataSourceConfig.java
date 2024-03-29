package io.roach.bank.config;

import com.zaxxer.hikari.HikariDataSource;
import io.cockroachdb.jdbc.CockroachProperty;
import io.roach.bank.ApplicationModel;
import io.roach.bank.ProfileNames;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.postgresql.PGProperty;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    public static final String SQL_TRACE_LOGGER = "io.roach.bank.SQL_TRACE";

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationModel applicationModel;

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
            ds.addDataSourceProperty(CockroachProperty.RETRY_CONNECTION_ERRORS.getName(), "true");
            ds.addDataSourceProperty(CockroachProperty.RETRY_TRANSIENT_ERRORS.getName(), "true");
            ds.addDataSourceProperty(CockroachProperty.IMPLICIT_SELECT_FOR_UPDATE.getName(), applicationModel.isSelectForUpdate() + "");
        }

        // https://stackoverflow.com/questions/851758/java-enums-jpa-and-postgres-enums-how-do-i-make-them-work-together/43125099#43125099
        if (ProfileNames.acceptsPostgresSQL(environment)) {
            ds.addDataSourceProperty("stringtype", "unspecified");
        }

        return ds;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource dataSource = primaryDataSource();

        DefaultQueryLogEntryCreator creator = new DefaultQueryLogEntryCreator();
        creator.setMultiline(true);

        SLF4JQueryLoggingListener listener = new SLF4JQueryLoggingListener();
        listener.setLogger(LoggerFactory.getLogger(SQL_TRACE_LOGGER));
        listener.setLogLevel(SLF4JLogLevel.TRACE);
        listener.setQueryLogEntryCreator(creator);

        return new LazyConnectionDataSourceProxy(
                ProxyDataSourceBuilder.create(dataSource)
                        .name("SQL-Trace")
                        .asJson()
                        .listener(listener)
                        .build());
    }
}
