package io.roach.bank.web.admin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import ch.qos.logback.classic.Level;
import io.roach.bank.api.ConnectionPoolConfig;
import io.roach.bank.api.ConnectionPoolSize;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.config.DataSourceConfig;
import io.roach.bank.service.AccountService;
import io.roach.bank.service.TransactionService;
import io.roach.bank.web.support.ConnectionPoolConfigFactory;
import io.roach.bank.web.support.ConnectionPoolSizeFactory;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private HikariDataSource hikariDataSource;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass()).clearAll()).withRel("clear")
                .withTitle("Clear all accounts and transactions"));

        index.add(linkTo(methodOn(getClass()).databaseMetadata()).withRel("database-info")
                .withTitle("Database and JDBC driver metadata"));

        index.add(linkTo(methodOn(getClass())
                .getConnectionPoolSize())
                .withRel(LinkRelations.POOL_SIZE_REL)
                .withTitle("Connection pool size"));

        index.add(linkTo(methodOn(getClass())
                .getConnectionPoolConfig())
                .withRel(LinkRelations.POOL_CONFIG_REL)
                .withTitle("Connection pool config"));

        index.add(linkTo(methodOn(getClass())
                .toggleTraceLogging())
                .withRel(LinkRelations.TOGGLE_TRACE_LOG)
                .withTitle("Toggle SQL trace logging"));

        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("actuator").buildAndExpand().toUriString())
                .withRel(LinkRelations.ACTUATOR_REL)
                .withTitle("Spring boot actuators"));

        Arrays.asList("hikaricp.connections", "hikaricp.connections.acquire", "hikaricp.connections.active",
                "hikaricp.connections.idle", "hikaricp.connections.max", "hikaricp.connections.usage",
                "http.server.requests", "jdbc.connections.active", "jdbc.connections.idle", "jdbc.connections.max",
                "jdbc.connections.min", "jvm.threads.live", "jvm.threads.peak", "process.cpu.usage", "system.cpu.count",
                "system.cpu.usage", "system.load.average.1m").forEach(key -> {
            index.add(
                    Link.of(ServletUriComponentsBuilder.fromCurrentContextPath().pathSegment("actuator", "metrics", key)
                                    .buildAndExpand().toUriString()).withRel(LinkRelations.ACTUATOR_REL)
                            .withTitle("Metrics endpoint"));
        });

        return ResponseEntity.ok(index);
    }

    @GetMapping(value = "/database-info")
    public ResponseEntity<Map<String, Object>> databaseMetadata() {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("databaseVersion", databaseVersion());

        Connection connection = null;
        try {
            connection = DataSourceUtils.doGetConnection(dataSource);
            DatabaseMetaData metaData = connection.getMetaData();

            properties.put("databaseProductName", metaData.getDatabaseProductName());
            properties.put("databaseMajorVersion", metaData.getDatabaseMajorVersion());
            properties.put("databaseMinorVersion", metaData.getDatabaseMinorVersion());
            properties.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            properties.put("driverMajorVersion", metaData.getDriverMajorVersion());
            properties.put("driverMinorVersion", metaData.getDriverMinorVersion());
            properties.put("driverName", metaData.getDriverName());
            properties.put("driverVersion", metaData.getDriverVersion());
            properties.put("maxConnections", metaData.getMaxConnections());
            properties.put("defaultTransactionIsolation", metaData.getDefaultTransactionIsolation());
            properties.put("transactionIsolation", connection.getTransactionIsolation());
            properties.put("transactionIsolationName",
                    ConnectionProviderInitiator.toIsolationNiceName(connection.getTransactionIsolation()));
        } catch (SQLException ex) {
            // Ignore
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        return ResponseEntity.ok(properties);
    }

    private String databaseVersion() {
        try {
            return new JdbcTemplate(dataSource).queryForObject("select version()", String.class);
        } catch (DataAccessException e) {
            return "unknown";
        }
    }

    @PostMapping(value = "/clear")
    @TransactionBoundary
    public ResponseEntity<String> clearAll() {
        logger.warn("Deleting all transactions..");
        transactionService.deleteAll();

        logger.warn("Deleting all accounts..");
        accountService.deleteAll();

        return ResponseEntity.ok("All transactions and accounts deleted");
    }

    @GetMapping(value = "/pool-size")
    public ResponseEntity<ConnectionPoolSize> getConnectionPoolSize() {
        HikariPoolMXBean mxBean = hikariDataSource.getHikariPoolMXBean();
        return ResponseEntity.ok(ConnectionPoolSizeFactory.from(mxBean)
                .add(linkTo(methodOn(getClass())
                        .getConnectionPoolConfig())
                        .withRel(LinkRelations.POOL_CONFIG_REL))
                .add(linkTo(methodOn(getClass())
                        .getConnectionPoolSize())
                        .withSelfRel()));
    }

    @PostMapping(value = "/pool-size")
    public ResponseEntity<MessageModel> setConnectionPoolSize(
            @RequestParam(value = "size", defaultValue = "50") int size) {
        hikariDataSource.setMaximumPoolSize(size);
        hikariDataSource.setMinimumIdle(size);
        logger.info("Setting pool size to {}", size);
        return ResponseEntity.ok(new MessageModel("ok")
                .add(linkTo(methodOn(getClass())
                        .getConnectionPoolConfig())
                        .withRel(LinkRelations.POOL_CONFIG_REL))
                .add(linkTo(methodOn(getClass())
                        .getConnectionPoolSize())
                        .withSelfRel()));
    }

    @GetMapping(value = "/pool-config")
    public ResponseEntity<ConnectionPoolConfig> getConnectionPoolConfig() {
        HikariConfigMXBean mxConfigBean = hikariDataSource.getHikariConfigMXBean();
        return ResponseEntity.ok(ConnectionPoolConfigFactory.from(mxConfigBean)
                .add(linkTo(methodOn(getClass())
                        .getConnectionPoolSize())
                        .withRel(LinkRelations.POOL_SIZE_REL))
                .add(linkTo(methodOn(getClass())
                        .getConnectionPoolConfig())
                        .withSelfRel()));
    }

    @PostMapping(value = "/toggle-trace")
    public ResponseEntity<MessageModel> toggleTraceLogging() {
        boolean enabled = toggleLogLevel(DataSourceConfig.SQL_TRACE_LOGGER, Level.TRACE);
        logger.info("SQL Trace Logging {}", enabled ? "ENABLED" : "DISABLED");

        return ResponseEntity.ok(
                new MessageModel("SQL Trace Logging " + (enabled ? "ENABLED" : "DISABLED"))
                        .add(linkTo(methodOn(getClass())
                                .toggleTraceLogging())
                                .withRel(LinkRelations.TOGGLE_TRACE_LOG))
                        .add(linkTo(methodOn(getClass())
                                .getConnectionPoolSize())
                                .withSelfRel()));

    }

    private boolean toggleLogLevel(String name, Level traceLevel) {
        ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory
                .getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(name);
        if (logger.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.DEBUG)) {
            logger.setLevel(traceLevel);
            return true;
        } else {
            logger.setLevel(Level.DEBUG);
            return false;
        }
    }

}
