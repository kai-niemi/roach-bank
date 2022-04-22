package io.roach.bank.client.command;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import io.roach.bank.api.BankLinkRelations;

import static io.roach.bank.api.BankLinkRelations.ACCOUNT_SUMMARY_REL;
import static io.roach.bank.api.BankLinkRelations.CITIES_REL;
import static io.roach.bank.api.BankLinkRelations.META_REL;
import static io.roach.bank.api.BankLinkRelations.REGION_CITIES_REL;
import static io.roach.bank.api.BankLinkRelations.REPORTING_REL;
import static io.roach.bank.api.BankLinkRelations.TRANSACTION_SUMMARY_REL;

@ShellComponent
@ShellCommandGroup(Constants.API_REPORTING_COMMANDS)
public class Report extends RestCommandSupport {
    @ShellMethod(value = "List city to region mappings", key = {"lm", "list-metadata"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listMetadata() {
        logger.info("Region groups");
        Map result = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(REGION_CITIES_REL))
                .toObject(Map.class);
        result.forEach((k, v) -> logger.info("{} -> {}", k, v));

        ResponseEntity<List> entity = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(CITIES_REL))
                .toEntity(List.class);
        logger.info("Local cities: {}", entity.getBody());
    }

    @ShellMethod(value = "Report account summary", key = {"rta", "report-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts() {
        ResponseEntity<List> accountSummary = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(REPORTING_REL))
                .follow(BankLinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .toEntity(List.class);

        accountSummary.getBody().forEach(item -> logger.info("{}", item));
    }

    @ShellMethod(value = "Report transaction summary", key = {"rtx", "report-txn"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions() {
        ResponseEntity<List> transactionSummary = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(REPORTING_REL))
                .follow(BankLinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .toEntity(List.class);

        transactionSummary.getBody().forEach(item -> logger.info("{}", item));
    }
}