package io.roach.bank.client.command;

import java.time.Duration;
import java.util.Currency;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.DurationFormat;

import static io.roach.bank.api.BankLinkRelations.ACCOUNT_BATCH_REL;
import static io.roach.bank.api.BankLinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class CreateAccounts extends RestCommandSupport {
    @ShellMethod(value = "Create new accounts", key = {"accounts", "a"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void accounts(
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "batch size", defaultValue = "128") int batchSize
    ) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            return;
        }

        Duration runtimeDuration = DurationFormat.parseDuration(duration);

        Link submitLink = traverson.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

//        concurrencyHelper.submitForDuration(() -> {
//                    String region = RandomData.selectRandom(regionMap.keySet());
//
//                    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
//                            .queryParam("region", region)
//                            .queryParam("prefix", "batch-" + batchNumber.incrementAndGet())
//                            .queryParam("batchSize", batchSize);
//
//                    ResponseEntity<String> response =
//                            restTemplate
//                                    .exchange(builder.build().toUri(), HttpMethod.POST, new HttpEntity<>(null),
//                                            String.class);
//
//                    if (!response.getStatusCode().is2xxSuccessful()) {
//                        logger.warn("Unexpected HTTP status: {}", response.toString());
//                    }
//                    return null;
//                },
//                "create-accounts", runtimeDuration);

    }
}
