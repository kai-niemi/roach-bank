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
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.service.TransactionService;

@RestController
@RequestMapping(value = "/api/transactionitem")
public class TransactionItemController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionItemResourceAssembler transactionItemResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<TransactionItem> transactionItemPagedResourcesAssembler;

    @GetMapping(value = "/{transactionId}/{region}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public PagedModel<TransactionItemModel> getTransactionItems(
            @PathVariable("transactionId") UUID transactionId,
            @PathVariable(value = "region", required = false) String region,
            @PageableDefault(size = 5) Pageable page) {
        Page<TransactionItem> entities = transactionService.findItemsByTransactionId(
                Transaction.Id.of(transactionId, region), page);
        PagedModel<TransactionItemModel> resources = transactionItemPagedResourcesAssembler
                .toModel(entities, transactionItemResourceAssembler);
        return resources;
    }

    @GetMapping(value = "/{transactionId}/{transactionRegion}/{accountId}/{accountRegion}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public TransactionItemModel getTransactionLeg(
            @PathVariable("transactionId") UUID transactionId,
            @PathVariable(value = "transactionRegion", required = false) String transactionRegion,
            @PathVariable("accountId") UUID accountId,
            @PathVariable(value = "accountRegion", required = false) String accountRegion) {
        TransactionItem.Id id = TransactionItem.Id.of(
                Account.Id.of(accountId, accountRegion),
                Transaction.Id.of(transactionId, transactionRegion));
        TransactionItem entity = transactionService.getItemById(id);
        if (entity == null) {
            throw new IllegalArgumentException("Transaction item not found: " + id);
        }
        return transactionItemResourceAssembler.toModel(entity);
    }
}
