package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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
import io.roach.bank.client.support.TaskDuration;
import io.roach.bank.client.support.TimeDuration;
import io.roach.bank.client.util.DurationFormat;

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

        final int concurrencyLevel = concurrency > 0 ? concurrency :
                Math.max(1, Runtime.getRuntime().availableProcessors() * 2 / regionMap.size());

        final List<Link> links = new ArrayList<>();

        accountMap.forEach((key, value) -> {
            value.forEach(accountModel -> {
                links.add(accountModel.getLink(followerReads
                        ? withCurie(BankLinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                        : withCurie(BankLinkRelations.ACCOUNT_BALANCE_REL))
                        .get());
            });
        });

        final TaskDuration taskDuration =
                TimeDuration.of(DurationFormat.parseDuration(duration));

        accountMap.forEach((key, value) -> IntStream.range(0, concurrencyLevel)
                .forEach(i -> throttledExecutor.submit(() -> randomRead(links),
                        taskDuration,
                        key + " balance")
                ));

        console.info("Max accounts per region: %d", accountLimit);
        console.info("Use follower reads: %s", followerReads);
        console.info("Concurrency level per region: %d", concurrencyLevel);
        console.info("Execution duration: %s", duration);
    }

    private String randomRead(List<Link> links) {
        Link link = RandomData.selectRandom(links);

        ResponseEntity<String> response = restTemplate.exchange(link.toUri(), HttpMethod.GET,
                new HttpEntity<>(null), String.class);

        return response.getBody();
    }
}
