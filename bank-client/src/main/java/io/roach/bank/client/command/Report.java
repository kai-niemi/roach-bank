package io.roach.bank.client.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import io.roach.bank.api.LinkRelations;

import static io.roach.bank.api.LinkRelations.ACCOUNT_SUMMARY_REL;
import static io.roach.bank.api.LinkRelations.CITIES_REL;
import static io.roach.bank.api.LinkRelations.META_REL;
import static io.roach.bank.api.LinkRelations.REGIONS_REL;
import static io.roach.bank.api.LinkRelations.REGION_CITIES_REL;
import static io.roach.bank.api.LinkRelations.REPORTING_REL;
import static io.roach.bank.api.LinkRelations.TRANSACTION_SUMMARY_REL;

@ShellComponent
@ShellCommandGroup(Constants.API_REPORTING_COMMANDS)
public class Report extends RestCommandSupport {
    @ShellMethod(value = "List all regions", key = {"lr", "list-regions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        logger.info("-- regions --");
        RestCommands restCommands = new RestCommands(traversonHelper);
        restCommands.getRegions().forEach(s -> {
            logger.info("{}", s);
        });
    }

    @ShellMethod(value = "List all cities", key = {"lc", "list-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listCities() {
        logger.info("-- cities --");
        RestCommands restCommands = new RestCommands(traversonHelper);
        restCommands.getCities().forEach(s -> {
            logger.info("{}", s);
        });
    }

    @ShellMethod(value = "List all region cities", key = {"lrc", "list-region-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities() {
        logger.info("-- region cities --");
        RestCommands restCommands = new RestCommands(traversonHelper);
        restCommands.getRegionCities("").forEach(s -> {
            logger.info("{}", s);
        });
    }

    @ShellMethod(value = "Report account summary", key = {"rta", "report-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts() {
        ResponseEntity<List> accountSummary = traversonHelper.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .toEntity(List.class);

        accountSummary.getBody().forEach(item -> logger.info("{}", item));
    }

    @ShellMethod(value = "Report transaction summary", key = {"rtx", "report-txn"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions() {
        ResponseEntity<List> transactionSummary = traversonHelper.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .toEntity(List.class);

        transactionSummary.getBody().forEach(item -> logger.info("{}", item));
    }
}
