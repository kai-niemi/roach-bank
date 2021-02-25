package io.roach.bank.client.command;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import io.roach.bank.api.BankLinkRelations;

import static io.roach.bank.api.BankLinkRelations.*;

@ShellComponent
@ShellCommandGroup(Constants.API_REPORTING_COMMANDS)
public class Report extends RestCommandSupport {
    @ShellMethod(value = "Region mappings", key = {"r", "regions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportRegions() {
        Map result = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(REGION_GROUPS_REL))
                .toObject(Map.class);
        console.info("Region mappings (%d)", result.size());
        result.forEach((k, v) -> console.info(" %s -> %s", k, v));

        ResponseEntity<List> entity = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(LOCAL_REGIONS_REL))
                .toEntity(List.class);
        console.info("Local region:\n%s", entity.getBody());
    }

    @ShellMethod(value = "Account summary report", key = {"rs", "report-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts() {
        ResponseEntity<List> accountSummary = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(REPORTING_REL))
                .follow(BankLinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .toEntity(List.class);

        accountSummary.getBody().forEach(item -> console.debug("%s", item));
    }

    @ShellMethod(value = "Transaction summary report", key = {"rx", "report-txn"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions() {
        ResponseEntity<List> transactionSummary = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(REPORTING_REL))
                .follow(BankLinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .toEntity(List.class);

        transactionSummary.getBody().forEach(item -> console.debug("%s", item));
    }
}
