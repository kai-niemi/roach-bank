package io.roach.bank.client.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.AccountBatchForm;
import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.provider.RegionProvider;

import static io.roach.bank.api.LinkRelations.ACCOUNT_BATCH_FORM_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class CreateAccounts extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Create new accounts", key = {"create-accounts"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = "number of accounts", defaultValue = "50_000") String totalAccounts,
            @ShellOption(help = "batch size", defaultValue = "128") int batchSize,
            @ShellOption(help = "initial balance per account", defaultValue = "25000.00") String balance,
            @ShellOption(help = "account currency", defaultValue = "USD") String currency,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY,
                    valueProvider = RegionProvider.class) String regions
    ) {
        final Set<String> regionSet = StringUtils.commaDelimitedListToSet(regions);

        final Map<String, Object> parameters = new HashMap<>();
        if (regionSet.isEmpty()) {
            regionSet.add(restCommands.getGatewayRegion());
            console.warnf("No region(s) specified - defaulting to gateway region %s", regionSet);
        }
        parameters.put("regions", regionSet);

        final Set<String> cities = new HashSet<>();
        cities.addAll(restCommands.getRegionCities(regionSet));

        logger.info("Cities found matching region(s) {}: {}", regionSet, cities);

        final Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_FORM_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        final int totAccounts = Integer.parseInt(totalAccounts.replace("_", ""));
        final int totBatches = Math.max(1, totAccounts / batchSize / cities.size());

        for (String city : cities) {
            Runnable worker = () -> {
                AccountBatchForm form = new AccountBatchForm();
                form.setCity(city);
                form.setPrefix("" + batchNumber.incrementAndGet());
                form.setBalance(balance);
                form.setCurrency(currency);
                form.setNumAccounts(totAccounts);
                form.setBatchSize(batchSize);

                ResponseEntity<String> response = restCommands.post(submitLink, form, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("Unexpected HTTP status: {}", response);
                }
            };

            executorTemplate.runAsync(city + " (accounts)", worker, totBatches);

            logger.info("Creating {} accounts for city '{}' in {} batches",
                    totAccounts, city, totBatches);
        }
    }
}
