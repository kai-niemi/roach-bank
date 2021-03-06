package io.roach.bank.web.api;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.service.BankService;
import io.roach.bank.web.support.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BankService bankService;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass())
                .clearAll())
                .withRel("clear")
                .withTitle("Clear all accounts and transactions"));

        index.add(linkTo(methodOn(getClass())
                .databaseMetadata())
                .withRel("database-info")
                .withTitle("Database and JDBC driver metadata"));

        index.add(Link.of(
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .pathSegment("actuator")
                        .buildAndExpand()
                        .toUriString()
        ).withRel(BankLinkRelations.ACTUATOR_REL)
                .withTitle("Spring boot actuators"));

        Arrays.asList(
                "bank.events.lost",
                "bank.events.queued",
                "bank.events.sent",
                "bank.txn.abort",
                "bank.txn.retry",
                "bank.txn.success",
                "hikaricp.connections",
                "hikaricp.connections.acquire",
                "hikaricp.connections.active",
                "hikaricp.connections.idle",
                "hikaricp.connections.max",
                "hikaricp.connections.usage",
                "http.server.requests",
                "jdbc.connections.active",
                "jdbc.connections.idle",
                "jdbc.connections.max",
                "jdbc.connections.min",
                "jetty.threads.busy",
                "jetty.threads.current",
                "jetty.threads.idle",
                "jvm.memory.max",
                "jvm.memory.used",
                "jvm.threads.live",
                "jvm.threads.peak",
                "process.cpu.usage",
                "system.cpu.count",
                "system.cpu.usage",
                "system.load.average.1m")
                .forEach(key -> {
                    index.add(Link.of(
                            ServletUriComponentsBuilder
                                    .fromCurrentContextPath()
                                    .pathSegment("actuator", "metrics", key)
                                    .buildAndExpand()
                                    .toUriString()
                    ).withRel(BankLinkRelations.ACTUATOR_REL)
                            .withTitle("Metrics endpoint"));
                });

        return ResponseEntity.ok(index);
    }

    @GetMapping(value = "/database-info")
    public ResponseEntity<Map<String, Object>> databaseMetadata() {
        final Map<String, Object> properties = new TreeMap<>();
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
    @TransactionBoundary(priority = TransactionBoundary.Priority.low)
    @Async
    public CompletableFuture<Void> clearAll() {
        logger.warn("Deleting all data");
        bankService.deleteAll();
        logger.info("Deleting all data - done");
        return CompletableFuture.completedFuture(null);
    }
}
