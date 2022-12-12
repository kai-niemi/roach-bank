package io.roach.bank.web.api;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.jdbc.spring.annotations.TimeTravel;
import io.cockroachdb.jdbc.spring.annotations.TransactionBoundary;
import io.cockroachdb.jdbc.spring.aspect.TimeTravelMode;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.service.NoSuchTransactionException;
import io.roach.bank.service.TransactionService;
import io.roach.bank.web.support.ZoomExpression;

import static io.roach.bank.api.LinkRelations.TRANSACTION_ITEMS_REL;
import static io.roach.bank.api.LinkRelations.withCurie;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/transaction")
public class TransactionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TransactionService bankService;

    @Autowired
    private TransactionResourceAssembler transactionResourceAssembler;

    @Autowired
    private TransactionItemResourceAssembler transactionItemResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Transaction> pagedTransactionResourceAssembler;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel("Monetary transaction resource");

        index.add(linkTo(methodOn(TransactionController.class)
                .index())
                .withSelfRel()
                .withTitle("Trasaction resource"));

        index.add(Link.of(UriTemplate.of(linkTo(TransactionController.class)
                        .toUriComponentsBuilder().path(
                                "/list/{?page,size}")  // RFC-6570 template
                        .build().toUriString()),
                LinkRelations.TRANSACTION_LIST_REL
        ).withTitle("Collection of transactions"));

        index.add(linkTo(methodOn(TransactionController.class)
                .listTransactions(PageRequest.of(0, 5, Sort.Direction.DESC, "id")))
                .withRel(LinkRelations.TRANSACTION_LIST_REL
                ).withTitle("Collection of transactions"));

        index.add(Link.of(UriTemplate.of(linkTo(TransactionFormController.class)
                        .toUriComponentsBuilder().path(
                                "/form/{?limit,amount,regions}")  // RFC-6570 template
                        .build().toUriString()),
                LinkRelations.TRANSACTION_FORM_REL
        ).withTitle("Form template for creating a transfer request"));

        return index;
    }

    @GetMapping(value = "/list")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public PagedModel<TransactionModel> listTransactions(@PageableDefault(size = 5) Pageable page) {
        return pagedTransactionResourceAssembler
                .toModel(bankService.find(page), transactionResourceAssembler);
    }

    @GetMapping(value = "/{id}")
    @TransactionBoundary(readOnly = true)
    public TransactionModel getTransaction(
            @PathVariable("id") UUID id) {
        Transaction entity = bankService.findById(id);
        if (entity == null) {
            throw new NoSuchTransactionException(id.toString());
        }

        TransactionModel resource = transactionResourceAssembler.toModel(entity);

        if (ZoomExpression.ofCurrentRequest().containsRel(withCurie(TRANSACTION_ITEMS_REL))) {
            Page<TransactionItem> entities = bankService.findItemsByTransactionId(id, PageRequest.of(0, 10));
            resource.setTransactionItems(transactionItemResourceAssembler.toCollectionModel(entities.getContent()));
        }

        return resource;
    }
}
