package io.roach.bank.client.command;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import io.roach.bank.api.BankLinkRelations;

import static io.roach.bank.api.BankLinkRelations.ACCOUNT_SUMMARY_REL;
import static io.roach.bank.api.BankLinkRelations.REPORTING_REL;
import static io.roach.bank.api.BankLinkRelations.TRANSACTION_SUMMARY_REL;

@ShellComponent
@ShellCommandGroup(Constants.API_REPORTING_COMMANDS)
public class Report extends RestCommandSupport {
    @ShellMethod(value = "Print account summary report")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts() {
        ResponseEntity<List> accountSummary = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(REPORTING_REL))
                .follow(BankLinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .toEntity(List.class);

        console.info("%s", accountSummary.getStatusCode());
        accountSummary.getBody().forEach(item -> console.debug("%s", item));
    }

    @ShellMethod(value = "Print transaction summary report")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions() {
        ResponseEntity<List> transactionSummary = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(REPORTING_REL))
                .follow(BankLinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .toEntity(List.class);

        console.info("%s", transactionSummary.getStatusCode());
        transactionSummary.getBody().forEach(item -> console.debug("%s", item));
    }
}
