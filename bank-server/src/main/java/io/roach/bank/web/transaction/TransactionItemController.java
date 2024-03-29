package io.roach.bank.web.transaction;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TimeTravelMode;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.TransactionItemModel;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.service.TransactionService;

@RestController
@RequestMapping(value = "/api/transaction/item")
public class TransactionItemController {
    @Autowired
    private TransactionService bankService;

    @Autowired
    private TransactionItemResourceAssembler transactionItemResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<TransactionItem> transactionItemPagedResourcesAssembler;

    @GetMapping(value = "/{transactionId}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    @Retryable
    public PagedModel<TransactionItemModel> getTransactionItems(
            @PathVariable("transactionId") UUID transactionId,
            @PageableDefault(size = 5) Pageable page) {
        Page<TransactionItem> entities = bankService.findItemsByTransactionId(transactionId, page);
        return transactionItemPagedResourcesAssembler
                .toModel(entities, transactionItemResourceAssembler);
    }

    @GetMapping(value = "/{transactionId}/{accountId}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    @Retryable
    public TransactionItemModel getTransactionLeg(
            @PathVariable("transactionId") UUID transactionId,
            @PathVariable("accountId") UUID accountId) {
        return transactionItemResourceAssembler.toModel(bankService.findItemById(transactionId, accountId));
    }
}
