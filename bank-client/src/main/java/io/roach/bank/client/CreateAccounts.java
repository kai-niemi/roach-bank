package io.roach.bank.client;

import java.util.Currency;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.ExecutorTemplate;
import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.ACCOUNT_BATCH_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class CreateAccounts extends CommandSupport {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Create new accounts", key = {"create-accounts", "ca"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = "create accounts for a time period", defaultValue = "") String duration,
            @ShellOption(help = "create a total number of accounts", defaultValue = "1_000_000") String totalAccounts,
            @ShellOption(help = "batch size", defaultValue = "1024") int batchSize,
            @ShellOption(help = "batch statement size", defaultValue = "32") int statementSize,
            @ShellOption(help = "initial balance per account", defaultValue = "500000.00") String balance,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.CITIES_HELP, defaultValue = Constants.EMPTY) String cities
    ) {
        final Set<String> cityNames = new HashSet<>();
        if (!regions.equals(Constants.EMPTY)) {
            cityNames.addAll(restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)));
        }
        if (!cities.equals(Constants.EMPTY)) {
            cityNames.addAll(StringUtils.commaDelimitedListToSet(cities));
        }

        final Map<String, Currency> cityCurrencyMap = restCommands.getCityCurrency();
        if (cityNames.isEmpty()) {
            cityNames.addAll(cityCurrencyMap.keySet());
        }

        final Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        for (String city : cityCurrencyMap.keySet()) {
            if (cityNames.contains(city)) {
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
                    logger.info("Creating accounts in '{}' for duration of {}", city, duration);
                    executorTemplate.runAsync(city, worker, DurationFormat.parseDuration(duration));
                } else {
                    final int totAccounts = Integer.parseInt(totalAccounts.replace("_", ""));
                    int iterations = Math.max(1, totAccounts / batchSize / cityCurrencyMap.size());
                    logger.info("Creating {} accounts in '{}' in {} iterations", city, totAccounts / cityCurrencyMap.size(), iterations);
                    executorTemplate.runAsync(city, worker, iterations);
                }
            } else {
                logger.info("Skipping {}", city);
            }
        }
    }
}
