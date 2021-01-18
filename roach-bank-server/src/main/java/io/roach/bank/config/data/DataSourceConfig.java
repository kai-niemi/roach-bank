package io.roach.bank.config.data;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Configuration
public class DataSourceConfig {
    protected final Logger logger = LoggerFactory.getLogger("io.roach.sql_trace");

    @Bean
    @Primary
    @ConfigurationProperties("roachbank.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("roachbank.datasource.configuration")
    public HikariDataSource primaryDataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("roachbank.datasource")
    public DataSource proxiedDataSource(HikariDataSource dataSource) {
        logger.info("Connection pool max size: {}", dataSource.getMaximumPoolSize());

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
