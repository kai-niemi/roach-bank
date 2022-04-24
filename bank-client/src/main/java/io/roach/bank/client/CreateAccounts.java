package io.roach.bank.client;

import java.util.Currency;
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

    @ShellMethod(value = "Create new accounts", key = {"accounts", "a"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "initial balance per account", defaultValue = "100000.00") String balance,
            @ShellOption(help = "total number of accounts", defaultValue = "1024") int numAccounts,
            @ShellOption(help = "batch size", defaultValue = "32") int batchSize,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.CITIES_HELP, defaultValue = Constants.EMPTY) String cities
    ) {
        final Set<String> cityNames = new HashSet<>();
        cityNames.addAll(restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)));
        cityNames.addAll(StringUtils.commaDelimitedListToSet(cities));

        final Map<String, Currency> cityCurrencyMap = restCommands.getCityCurrency();
        if (cityNames.isEmpty()) {
            cityNames.addAll(cityCurrencyMap.keySet());
        }

        final Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        cityCurrencyMap.keySet().forEach(city -> {
            if (cityNames.contains(city)) {
                logger.info("Processing {}", city);

                executorTemplate.runAsync(city, () -> {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                            .queryParam("city", city)
                            .queryParam("prefix", "" + batchNumber.incrementAndGet())
                            .queryParam("numAccounts", numAccounts)
                            .queryParam("balance", balance)
                            .queryParam("batchSize", batchSize);

                    ResponseEntity<String> response = restCommands.post(Link.of(builder.build().toUriString()));
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        logger.warn("Unexpected HTTP status: {}", response);
                    }
                }, DurationFormat.parseDuration(duration));
            } else {
                logger.info("Skipping {}", city);
            }
        });
    }
}
