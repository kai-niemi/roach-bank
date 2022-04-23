package io.roach.bank.client.command;

import java.util.Currency;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.ExecutorTemplate;

import static io.roach.bank.api.LinkRelations.ACCOUNT_BATCH_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class CreateAccounts extends RestCommandSupport {
    @Autowired
    protected ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Create new accounts", key = {"accounts", "a"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "initial balance per account", defaultValue = "100000.00") String balance,
            @ShellOption(help = "number of accounts per request", defaultValue = "320") int numAccounts,
            @ShellOption(help = "batch size per request", defaultValue = "32") int batchSize
    ) {
        RestCommands restCommands = new RestCommands(traversonHelper);

        final Map<String, Currency> cityCurrencyMap = restCommands.getCityCurrency();

        final Link submitLink = traversonHelper.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        cityCurrencyMap.keySet().forEach(city -> {
            executorTemplate.runAsync("create-accounts " + city, () -> {
                UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                        .queryParam("city", city)
                        .queryParam("prefix", "" + batchNumber.incrementAndGet())
                        .queryParam("numAccounts", numAccounts)
                        .queryParam("balance", balance)
                        .queryParam("batchSize", batchSize);

                ResponseEntity<String> response =
                        restTemplate
                                .exchange(builder.build().toUri(), HttpMethod.POST, new HttpEntity<>(null),
                                        String.class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("Unexpected HTTP status: {}", response);
                }
            }, DurationFormat.parseDuration(duration));
        });
    }
}
