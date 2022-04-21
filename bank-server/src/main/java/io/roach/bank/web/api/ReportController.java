package io.roach.bank.web.api;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.repository.ReportingRepository;
import io.roach.bank.web.support.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/report")
public class ReportController {
    @Autowired
    private ReportingRepository reportingRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass())
                .getAccountSummary())
                .withRel(BankLinkRelations.ACCOUNT_SUMMARY_REL)
                .withTitle("Account business report"));

        index.add(linkTo(methodOn(getClass())
                .getTransactionSummary())
                .withRel(BankLinkRelations.TRANSACTION_SUMMARY_REL)
                .withTitle("Transaction business report"));

        return index;
    }

    @GetMapping(value = "/account-summary")
    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.SNAPSHOT_READ, interval = "-10s"),
            vectorize = TransactionBoundary.Vectorize.off,
            priority = TransactionBoundary.Priority.low)
    public Collection<AccountSummary> getAccountSummary() {
        Collection<AccountSummary> result = new LinkedList<>();
        metadataRepository.getCurrencyCities().forEach((currency, regions) -> {
            result.add(reportingRepository.accountSummary(currency));
        });
        return result;
    }

    @GetMapping(value = "/transaction-summary")
    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.SNAPSHOT_READ, interval = "-10s"),
            vectorize = TransactionBoundary.Vectorize.off,
            priority = TransactionBoundary.Priority.low)
    public Collection<TransactionSummary> getTransactionSummary() {
        Collection<TransactionSummary> result = new LinkedList<>();
        metadataRepository.getCurrencyCities().forEach((currency, regions) -> {
            result.add(reportingRepository.transactionSummary(currency));
        });
        return result;
    }
}
