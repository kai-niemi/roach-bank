package io.roach.bank.client.command;

import io.roach.bank.api.AccountBatchForm;
import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.HypermediaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.roach.bank.api.LinkRelations.ACCOUNT_BATCH_FORM_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class CreateAccounts extends AbstractCommand {
    @Autowired
    private HypermediaClient bankClient;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Create new accounts", key = {"accounts", "a"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = "number of accounts per city", defaultValue = "50_000") String accounts,
            @ShellOption(help = "batch size", defaultValue = "128") int batchSize,
            @ShellOption(help = "initial balance per account", defaultValue = "25000.00") String balance,
            @ShellOption(help = "account currency", defaultValue = "USD") String currency,
            @ShellOption(help = Constants.REGIONS_HELP,
                    defaultValue = Constants.DEFAULT_REGION,
                    valueProvider = RegionProvider.class) String region
    ) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("region", region);

        final Collection<String> cities = bankClient.getRegionCities(region);

        logger.info("Found {} cities: {}", cities.size(), cities);

        final Link submitLink = bankClient.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_FORM_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        final int numAccounts = Integer.parseInt(accounts.replace("_", ""));

        for (String city : cities) {
            Runnable worker = () -> {
                AccountBatchForm form = new AccountBatchForm();
                form.setCity(city);
                form.setPrefix("" + batchNumber.incrementAndGet());
                form.setBalance(balance);
                form.setCurrency(currency);
                form.setBatchSize(batchSize);

                ResponseEntity<String> response = bankClient.post(submitLink, form, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("Unexpected HTTP status: {}", response);
                }
            };

            logger.info("Creating {} accounts for city '{}' using {} batches",
                    numAccounts, city, numAccounts / batchSize);

            executorTemplate.runAsync(city + " (" + region + ")", worker, numAccounts / batchSize);
        }
    }
}
