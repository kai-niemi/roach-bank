package io.roach.bank.client.command;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.command.support.DurationFormat;
import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.HypermediaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Balance extends AbstractCommand {
    @Autowired
    private HypermediaClient bankClient;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Read account balance", key = {"b", "balance"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void balance(
            @ShellOption(help = "use follower reads", defaultValue = "false") boolean followerReads,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT)
            int limit,
            @ShellOption(help = Constants.REGIONS_HELP,
                    defaultValue = Constants.DEFAULT_REGION,
                    valueProvider = RegionProvider.class) String region,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration
    ) {
        Map<String, List<AccountModel>> accounts = bankClient.getTopAccounts(region, limit);
        accounts.forEach((city, accountModels) -> {
            logger.info("Found {} accounts in city [{}]", accountModels.size(), city);
            console.success(ListAccounts.printContentTable(accountModels));
        });

        accounts.forEach((city, accountModels) -> {
            final List<Link> links = new ArrayList<>();

            accountModels.forEach(accountModel -> links.add(accountModel.getLink(
                            followerReads
                                    ? withCurie(LinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                                    : withCurie(LinkRelations.ACCOUNT_BALANCE_REL))
                    .get()));

            executorTemplate.runAsync(region + " (" + city + ")",
                    () -> bankClient.get(RandomData.selectRandom(links)),
                    DurationFormat.parseDuration(duration));
        });
    }
}
