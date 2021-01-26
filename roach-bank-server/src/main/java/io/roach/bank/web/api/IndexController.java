package io.roach.bank.web.api;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.roach.bank.Application;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.web.support.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Index controller for the Hypermedia API that provides links to all key
 * resources provided in the API.
 */
@RestController
@RequestMapping(value = "/api")
public class IndexController {
    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage(readMessageOfTheDay());
        index.add(linkTo(methodOn(AccountController.class)
                .index())
                .withRel(BankLinkRelations.ACCOUNT_REL)
                .withTitle("Account resource details")
        );
        index.add(linkTo(methodOn(TransactionController.class)
                .index())
                .withRel(BankLinkRelations.TRANSACTION_REL)
                .withTitle("Transaction resource details")
        );
        index.add(linkTo(methodOn(ReportController.class)
                .index())
                .withRel(BankLinkRelations.REPORTING_REL)
                .withTitle("Reporting resource details")
        );
//        index.add(linkTo(methodOn(AdminController.class)
//                .index())
//                .withRel(BankLinkRelations.ADMIN_REL)
//                .withTitle("Admin resource details")
//        );
        index.add(linkTo(methodOn(MetadataController.class)
                .index())
                .withRel(BankLinkRelations.META_REL)
                .withTitle("Metadata resource details")
        );
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

        return ResponseEntity.ok(index);
    }

    private String readMessageOfTheDay() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bas);
        Banner banner = new ResourceBanner(new ClassPathResource("motd.txt"));
        banner.printBanner(environment, Application.class, ps);
        ps.flush();
        return new String(bas.toByteArray());
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
}
