package io.roach.bank.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.ExecutorTemplate;
import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.MAIN_COMMANDS)
public class ReadBalance extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Query account balances", key = {"b", "balance"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void balance(
            @ShellOption(help = "use non-authoritative follower reads", defaultValue = "false") boolean followerReads,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT)
            int limit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration
    ) {
        Map<String, List<AccountModel>> accounts = restCommands.getTopAccounts(
                StringUtils.commaDelimitedListToSet(regions), limit);
        if (accounts.isEmpty()) {
            logger.warn("No cities found matching: {}", regions);
        }

        accounts.forEach((city, accountModels) -> {
            final List<Link> links = new ArrayList<>();

            accountModels.forEach(accountModel -> {
                links.add(accountModel.getLink(followerReads
                                ? withCurie(LinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                                : withCurie(LinkRelations.ACCOUNT_BALANCE_REL))
                        .get());
            });

            executorTemplate.runAsync(city,
                    () -> readAccountBalance(RandomData.selectRandom(links)),
                    DurationFormat.parseDuration(duration));
        });
    }

    private void readAccountBalance(Link link) {
        restCommands.get(link);
    }
}
