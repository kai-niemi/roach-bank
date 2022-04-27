package io.roach.bank.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.support.CockroachFacts;
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
    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage(CockroachFacts.nextFact());

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
        index.add(linkTo(methodOn(ReportController.class)
                .index())
                .withRel(LinkRelations.REPORTING_REL)
                .withTitle("Reporting resource details")
        );
        index.add(linkTo(methodOn(MetadataController.class)
                .index())
                .withRel(LinkRelations.META_REL)
                .withTitle("Metadata resource details")
        );
        index.add(linkTo(methodOn(AdminController.class)
                .index())
                .withRel(LinkRelations.ADMIN_REL)
                .withTitle("Admin resource details")
        );

        return ResponseEntity.ok(index);
    }
}
