package io.roach.bank.web.api;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        index.add(linkTo(methodOn(MetadataController.class)
                .index())
                .withRel(BankLinkRelations.META_REL)
                .withTitle("Metadata resource details")
        );
        index.add(linkTo(methodOn(AdminController.class)
                .index())
                .withRel(BankLinkRelations.ADMIN_REL)
                .withTitle("Admin resource details")
        );

        return ResponseEntity.ok(index);
    }

    private String readMessageOfTheDay() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bas);
        Banner banner = new ResourceBanner(new ClassPathResource("motd.txt"));
        banner.printBanner(environment, Application.class, ps);
        ps.flush();
        return bas.toString();
    }
}
