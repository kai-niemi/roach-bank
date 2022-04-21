package io.roach.bank.config;

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
    protected final Logger traceLogger = LoggerFactory.getLogger("io.roach.SQL_TRACE");

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource primaryDataSource() {
        HikariDataSource dataSource = dataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        dataSource.addDataSourceProperty("reWriteBatchedInserts", "true");
        return dataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = primaryDataSource();

        if (traceLogger.isTraceEnabled()) {
            ChainListener listener = new ChainListener();
            listener.addListener(new DataSourceQueryCountListener());
            return new LazyConnectionDataSourceProxy(ProxyDataSourceBuilder
                    .create(dataSource)
                    .name("SQL-Trace")
                    .listener(listener)
                    .asJson()
                    .countQuery()
                    .logQueryBySlf4j(SLF4JLogLevel.TRACE, "io.roach.SQL_TRACE")
//                    .logSlowQueryBySlf4j(150, TimeUnit.MILLISECONDS)
//                    .multiline()
                    .build());
        }

        return new LazyConnectionDataSourceProxy(dataSource);
    }
}
