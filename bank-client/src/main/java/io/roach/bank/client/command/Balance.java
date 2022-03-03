package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.DurationFormat;

import static io.roach.bank.api.BankLinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class Balance extends RestCommandSupport {
    @ShellMethod(value = "Query account balances", key = {"b", "balance"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void balance(
            @ShellOption(help = "use non-authoritative follower reads", defaultValue = "false") boolean followerReads,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT)
                    int accountLimit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency
    ) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            return;
        }

        final Map<String, List<AccountModel>> accountMap = lookupAccounts(regionMap.keySet(), accountLimit);
        if (accountMap.isEmpty()) {
            return;
        }

        final int concurrencyFinal = concurrency <= 0 ?
                Math.max(2, regionMap.size() / Runtime.getRuntime().availableProcessors()) : concurrency;

        logger.info("Using concurrency level {}", concurrencyFinal);

        accountMap.forEach((regionKey, accountModels) -> {
            final List<Link> links = new ArrayList<>();

            accountModels.forEach(accountModel -> {
                links.add(accountModel.getLink(followerReads
                        ? withCurie(BankLinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                        : withCurie(BankLinkRelations.ACCOUNT_BALANCE_REL))
                        .get());
            });

            executorTemplate.runForDuration(boundedExecutor -> {
                boundedExecutor
                        .submitTask(() -> readAccountBalance(links),
                                regionKey + " - balance", concurrencyFinal);
            }, DurationFormat.parseDuration(duration));
        });

        logger.info("All {} workers queued", accountMap.size());
    }

    private String readAccountBalance(List<Link> links) {
        Link link = RandomData.selectRandom(links);

        ResponseEntity<String> response = restTemplate.exchange(link.toUri(), HttpMethod.GET,
                new HttpEntity<>(null), String.class);

        return response.getBody();
    }
}
