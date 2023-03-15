package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

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
import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.provider.RegionProvider;
import io.roach.bank.client.util.DurationFormat;

import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Balance extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Read account balance", key = {"b", "balance"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void balance(
            @ShellOption(help = "use follower reads", defaultValue = "false") boolean followerReads,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT)
            int limit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY,
                    valueProvider = RegionProvider.class) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "number of threads per city", defaultValue = "1") int concurrency
    ) {
        final Set<String> regionSet = StringUtils.commaDelimitedListToSet(regions);

        final Map<String, Object> parameters = new HashMap<>();
        if (regionSet.isEmpty()) {
            regionSet.add(restCommands.getGatewayRegion());
            console.warnf("No region(s) specified - defaulting to gateway region %s", regionSet);
        }
        parameters.put("regions", regionSet);

        Map<String, List<AccountModel>> accounts = restCommands.getTopAccounts(regionSet, limit);
        if (accounts.isEmpty()) {
            logger.warn("No cities found matching region(s): {}", regionSet);
        }

        accounts.forEach((city, accountModels) -> {
            final List<Link> links = new ArrayList<>();

            accountModels.forEach(accountModel -> links.add(accountModel.getLink(
                            followerReads
                                    ? withCurie(LinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                                    : withCurie(LinkRelations.ACCOUNT_BALANCE_REL))
                    .get()));

            IntStream.rangeClosed(1, concurrency).forEach(value -> {
                executorTemplate.runAsync(city + " (balance)",
                        () -> restCommands.get(RandomData.selectRandom(links)),
                        DurationFormat.parseDuration(duration));
            });
        });
    }
}
