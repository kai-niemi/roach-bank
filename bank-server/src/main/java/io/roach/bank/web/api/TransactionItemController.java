package io.roach.bank.web.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.TransactionItemModel;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.service.TransactionService;

@RestController
@RequestMapping(value = "/api/transactionitem")
public class TransactionItemController {
    @Autowired
    private TransactionService bankService;

    @Autowired
    private TransactionItemResourceAssembler transactionItemResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<TransactionItem> transactionItemPagedResourcesAssembler;

    @GetMapping(value = "/{transactionId}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public PagedModel<TransactionItemModel> getTransactionItems(
            @PathVariable("transactionId") UUID transactionId,
            @PageableDefault(size = 5) Pageable page) {
        Page<TransactionItem> entities = bankService.findItemsByTransactionId(
                transactionId, page);
        return transactionItemPagedResourcesAssembler
                .toModel(entities, transactionItemResourceAssembler);
    }

    @GetMapping(value = "/{transactionId}/{accountId}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public TransactionItemModel getTransactionLeg(
            @PathVariable("transactionId") UUID transactionId,
            @PathVariable("accountId") UUID accountId) {
        TransactionItem.Id id = TransactionItem.Id.of(accountId, transactionId);
        return transactionItemResourceAssembler.toModel(bankService.getItemById(id));
    }
}
