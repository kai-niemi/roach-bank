package io.roach.bank.config.data;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
public class DataSourceConfig {
    protected final Logger logger = LoggerFactory.getLogger("io.roach.SQL_TRACE");

    @Bean
    @Primary
    @ConfigurationProperties("roachbank.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("roachbank.datasource.configuration")
    public HikariDataSource primaryDataSource() {
        return dataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
//    @ConfigurationProperties("roachbank.datasource")
    public DataSource proxiedDataSource(HikariDataSource dataSource) {
        try {
            String version = new JdbcTemplate(dataSource).queryForObject("select version()", String.class);
            logger.info("Database version: {}", version);
        } catch (DataAccessException e) {
        }

        logger.info("Connection pool max size: {}", dataSource.getMaximumPoolSize());
        logger.info("Connection pool idle timeout: {}", dataSource.getIdleTimeout());
        logger.info("Connection pool max lifetime: {}", dataSource.getMaxLifetime());
        logger.info("Connection pool validation timeout: {}", dataSource.getValidationTimeout());

        if (logger.isDebugEnabled()) {
            ChainListener listener = new ChainListener();
            listener.addListener(new DataSourceQueryCountListener());

            return ProxyDataSourceBuilder
                    .create(new LazyConnectionDataSourceProxy(dataSource))
                    .name("SQL-Trace")
                    .listener(listener)
                    .asJson()
                    .countQuery()
                    .logQueryBySlf4j(SLF4JLogLevel.DEBUG, "io.roach.sql_trace")
//                    .logSlowQueryBySlf4j(50, TimeUnit.MILLISECONDS)
                    .multiline()
                    .build();
        }

        return new LazyConnectionDataSourceProxy(dataSource);
    }
}
