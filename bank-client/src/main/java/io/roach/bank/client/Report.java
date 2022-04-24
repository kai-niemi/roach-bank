package io.roach.bank.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.ACCOUNT_SUMMARY_REL;
import static io.roach.bank.api.LinkRelations.REPORTING_REL;
import static io.roach.bank.api.LinkRelations.TRANSACTION_SUMMARY_REL;

@ShellComponent
@ShellCommandGroup(Constants.API_REPORTING_COMMANDS)
public class Report extends CommandSupport {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private Console console;

    @ShellMethod(value = "Print gateway region", key = {"pg", "print-gateway-region"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.cyan("%s\n", restCommands.getGatewayRegion());
    }

    @ShellMethod(value = "List regions", key = {"lr", "list-regions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        console.yellow("-- regions --\n");
        restCommands.getRegions().forEach(s -> {
            console.cyan("%s\n", s);
        });
    }

    @ShellMethod(value = "List region cities", key = {"lrc", "list-region-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "") String regions) {
        console.yellow("-- region cities --\n");
        restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.cyan("%s\n", s);
        });
    }

    @ShellMethod(value = "List cities", key = {"lc", "list-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listCities() {
        console.yellow("-- cities --\n");
        restCommands.getCities().forEach(s -> {
            console.cyan("%s\n", s);
        });
    }

    @ShellMethod(value = "List city currency", key = {"lcc", "list-city-currency"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listCityCurrency() {
        console.yellow("-- city / currency --\n");
        restCommands.getCityCurrency().forEach((s, currency) -> {
            console.cyan("%s: %s\n", s, currency);
        });
    }

    @ShellMethod(value = "Report account summary", key = {"ra", "report-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts() {
        ResponseEntity<List> accountSummary = restCommands.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .toEntity(List.class);

        accountSummary.getBody().forEach(item -> console.cyan("%s\n", item));
    }

    @ShellMethod(value = "Report transaction summary", key = {"rx", "report-transactions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions() {
        ResponseEntity<List> transactionSummary = restCommands.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .toEntity(List.class);

        transactionSummary.getBody().forEach(item -> console.cyan("%s\n", item));
    }
}
