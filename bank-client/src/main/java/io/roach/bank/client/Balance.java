package io.roach.bank.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.AsyncHelper;
import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.HypermediaClient;

import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Balance extends AbstractCommand {
    @Autowired
    private HypermediaClient bankClient;

    @Autowired
    private AsyncHelper asyncHelper;

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
        logger.info("Find max %d accounts per city in region [%s]".formatted(limit, region));

        Map<String, List<AccountModel>> accounts = bankClient.getTopAccounts(region, limit);

        accounts.forEach((city, accountModels) -> {
            final List<Link> links = new ArrayList<>();

            accountModels.forEach(accountModel -> links.add(accountModel.getLink(
                            followerReads
                                    ? withCurie(LinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                                    : withCurie(LinkRelations.ACCOUNT_BALANCE_REL))
                    .get()));

            asyncHelper.runAsync(city + " (" + region + ")",
                    () -> bankClient.get(RandomData.selectRandom(links)),
                    DurationFormat.parseDuration(duration));
        });
    }
}
