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

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.service.NoSuchTransactionException;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.service.BankService;
import io.roach.bank.web.support.MessageModel;
import io.roach.bank.web.support.ZoomExpression;

import static io.roach.bank.api.BankLinkRelations.TRANSACTION_ITEMS_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/transaction")
public class TransactionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BankService bankService;

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
                BankLinkRelations.TRANSACTION_LIST_REL
        ).withTitle("Collection of transactions"));

        index.add(linkTo(methodOn(TransactionController.class)
                .listTransactions(PageRequest.of(0, 5, Sort.Direction.DESC, "id")))
                .withRel(BankLinkRelations.TRANSACTION_LIST_REL
                ).withTitle("Collection of transactions"));

        index.add(Link.of(UriTemplate.of(linkTo(TransactionFormController.class)
                        .toUriComponentsBuilder().path(
                        "/form/{?limit,amount,regions}")  // RFC-6570 template
                        .build().toUriString()),
                BankLinkRelations.TRANSACTION_FORM_REL
        ).withTitle("Form template for creating a transfer request"));

        return index;
    }

    @GetMapping(value = "/list")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public PagedModel<TransactionModel> listTransactions(
            @PageableDefault(size = 5) Pageable page) {
        return pagedTransactionResourceAssembler
                .toModel(bankService.find(page), transactionResourceAssembler);
    }

    @GetMapping(value = "/{id}/{region}")
    @TransactionBoundary(readOnly = true)
    public TransactionModel getTransaction(
            @PathVariable("id") UUID id,
            @PathVariable(value = "region", required = false) String region) {
        Transaction.Id txId = Transaction.Id.of(id, region);

        Transaction entity = bankService.findById(txId);
        if (entity == null) {
            throw new NoSuchTransactionException(txId.toString());
        }

        TransactionModel resource = transactionResourceAssembler.toModel(entity);

        if (ZoomExpression.ofCurrentRequest().containsRel(withCurie(TRANSACTION_ITEMS_REL))) {
            Page<TransactionItem> entities = bankService.findItemsByTransactionId(txId, PageRequest.of(0, 10));
            resource.setTransactionItems(transactionItemResourceAssembler.toCollectionModel(entities.getContent()));
        }

        return resource;
    }
}
