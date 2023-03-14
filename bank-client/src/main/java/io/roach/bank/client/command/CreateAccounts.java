package io.roach.bank.client.command;

import java.util.HashSet;
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
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.util.DurationFormat;

import static io.roach.bank.api.LinkRelations.ACCOUNT_BATCH_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class CreateAccounts extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Create new accounts", key = {"accounts", "a"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = "create accounts for a time period", defaultValue = "") String duration,
            @ShellOption(help = "create a total number of accounts", defaultValue = "50_000") String totalAccounts,
            @ShellOption(help = "batch size", defaultValue = "1024") int batchSize,
            @ShellOption(help = "batch statement size", defaultValue = "64") int statementSize,
            @ShellOption(help = "initial balance per account", defaultValue = "25000.00") String balance,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY,
                    valueProvider = RegionProvider.class) String regions
    ) {
        final Set<String> cities = new HashSet<>();
        cities.addAll(restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)));

        final Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        for (String city : cities) {
            if (cities.contains(city)) {
                Runnable worker = () -> {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                            .queryParam("city", city)
                            .queryParam("prefix", "" + batchNumber.incrementAndGet())
                            .queryParam("numAccounts", batchSize)
                            .queryParam("balance", balance)
                            .queryParam("batchSize", statementSize);

                    ResponseEntity<String> response = restCommands.post(Link.of(builder.build().toUriString()));
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        logger.warn("Unexpected HTTP status: {}", response);
                    }
                };

                if (StringUtils.hasLength(duration)) {
                    executorTemplate.runAsync(city + " (accounts) " + duration, worker,
                            DurationFormat.parseDuration(duration));

                    logger.info("Creating accounts in '{}' for duration of {}", city, duration);
                } else {
                    int totAccounts = Integer.parseInt(totalAccounts.replace("_", ""));
                    int iterations = Math.max(1, totAccounts / batchSize / cities.size());

                    executorTemplate.runAsync(city + " (create) " + iterations, worker, iterations);

                    logger.info("Creating {} accounts in '{}' in {} iterations", city,
                            totAccounts / cities.size(), iterations);
                }
            } else {
                logger.info("Skipping {}", city);
            }
        }
    }
}
