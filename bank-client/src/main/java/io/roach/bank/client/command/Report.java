package io.roach.bank.client.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.client.command.support.BankClient;

import static io.roach.bank.api.LinkRelations.ACCOUNT_SUMMARY_REL;
import static io.roach.bank.api.LinkRelations.REPORTING_REL;
import static io.roach.bank.api.LinkRelations.TRANSACTION_SUMMARY_REL;

@ShellComponent
@ShellCommandGroup(Constants.REPORTING_COMMANDS)
public class Report extends AbstractCommand {
    @Autowired
    private BankClient bankClient;

    @ShellMethod(value = "Report account summary", key = {"report-accounts", "ra"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportAccounts(@ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY,
            valueProvider = RegionProvider.class) String regions
    ) {
        final Set<String> regionSet = StringUtils.commaDelimitedListToSet(regions);

        final Map<String, Object> parameters = new HashMap<>();
        if (regionSet.isEmpty()) {
            regionSet.add(bankClient.getGatewayRegion());
            console.warnf("No region(s) specified - defaulting to gateway region %s", regionSet);
        }
        parameters.put("regions", regionSet);

        ResponseEntity<List> accountSummary = bankClient.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(ACCOUNT_SUMMARY_REL))
                .withTemplateParameters(parameters)
                .toEntity(List.class);

        accountSummary.getBody().forEach(item -> console.infof("%s", item));
    }

    @ShellMethod(value = "Report transaction summary", key = {"report-transactions", "rt"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void reportTransactions(@ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY,
            valueProvider = RegionProvider.class) String regions
    ) {
        final Set<String> regionSet = StringUtils.commaDelimitedListToSet(regions);

        final Map<String, Object> parameters = new HashMap<>();
        if (regionSet.isEmpty()) {
            regionSet.add(bankClient.getGatewayRegion());
            console.warnf("No region(s) specified - defaulting to gateway region %s", regionSet);
        }
        parameters.put("regions", regionSet);

        ResponseEntity<List> transactionSummary = bankClient.fromRoot()
                .follow(LinkRelations.withCurie(REPORTING_REL))
                .follow(LinkRelations.withCurie(TRANSACTION_SUMMARY_REL))
                .withTemplateParameters(parameters)
                .toEntity(List.class);

        transactionSummary.getBody().forEach(item -> console.infof("%s", item));
    }
}
