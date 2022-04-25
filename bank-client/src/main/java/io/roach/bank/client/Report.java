package io.roach.bank.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.ACCOUNT_SUMMARY_REL;
import static io.roach.bank.api.LinkRelations.REPORTING_REL;
import static io.roach.bank.api.LinkRelations.TRANSACTION_SUMMARY_REL;

@ShellComponent
@ShellCommandGroup(Constants.REPORTING_COMMANDS)
public class Report extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Report account summary", key = {"report-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts() {
        ResponseEntity<List> accountSummary = restCommands.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .toEntity(List.class);

        accountSummary.getBody().forEach(item -> console.cyan("%s\n", item));
    }

    @ShellMethod(value = "Report transaction summary", key = {"report-transactions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions() {
        ResponseEntity<List> transactionSummary = restCommands.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .toEntity(List.class);

        transactionSummary.getBody().forEach(item -> console.cyan("%s\n", item));
    }
}
