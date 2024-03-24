package io.roach.bank.web;

import io.roach.bank.web.account.AccountController;
import io.roach.bank.web.config.ConfigurationController;
import io.roach.bank.web.transaction.TransactionController;
import io.roach.bank.web.transaction.TransferController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Index controller for the Hypermedia API that provides links to all key
 * resources provided in the API.
 */
@RestController
@RequestMapping(value = "/api")
public class IndexController {
    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Welcome to text-only Roach Bank. You are in a dark, cold lobby.");

        index.add(linkTo(methodOn(AccountController.class)
                .index())
                .withRel(LinkRelations.ACCOUNT_REL)
                .withTitle("Account resource details")
        );
        index.add(linkTo(methodOn(TransactionController.class)
                .index())
                .withRel(LinkRelations.TRANSACTION_REL)
                .withTitle("Transaction resource details")
        );
        index.add(Link.of(UriTemplate.of(linkTo(TransferController.class)
                                .toUriComponentsBuilder().path(
                                        "/form{?limit,amount,regions}")  // RFC-6570 template
                                .build().toUriString()),
                        LinkRelations.TRANSFER_FORM_REL)
                .withTitle("Form template for creating a transfer request")
        );
        index.add(linkTo(methodOn(ReportController.class)
                .index())
                .withRel(LinkRelations.REPORTING_REL)
                .withTitle("Reporting resource details")
        );
        index.add(linkTo(methodOn(AdminController.class)
                .index())
                .withRel(LinkRelations.ADMIN_REL)
                .withTitle("Admin resource details")
        );
        index.add(linkTo(methodOn(ConfigurationController.class)
                .index())
                .withRel(LinkRelations.CONFIG_INDEX_REL)
                .withTitle("Configuration resource details")
        );

        return ResponseEntity.ok(index);
    }
}
