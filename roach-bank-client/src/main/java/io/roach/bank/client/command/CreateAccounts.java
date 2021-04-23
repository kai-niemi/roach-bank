package io.roach.bank.client.command;

import java.util.Currency;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
import io.roach.bank.client.support.CountDuration;
import io.roach.bank.client.support.TaskDuration;
import io.roach.bank.client.support.TimeDuration;
import io.roach.bank.client.util.DurationFormat;

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
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency,
            @ShellOption(help = "number of accounts rather than duration (if > 0)", defaultValue = "-1") int count,
            @ShellOption(help = "batch size", defaultValue = "128") int batchSize
    ) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            return;
        }

        final int concurrencyLevel = concurrency > 0 ? concurrency :
                Math.max(1, Runtime.getRuntime().availableProcessors() * 2 / regionMap.size());

        TaskDuration taskDuration;

        if (count > 0) {
            console.info("Creating %,d accounts in %,d batches of %,d", count, count/batchSize, batchSize);
            taskDuration = CountDuration.of(count / Math.max(batchSize, 1));
        } else {
            console.info("Creating accounts for %s in batches of %,d", duration, batchSize);
            taskDuration = TimeDuration.of(DurationFormat.parseDuration(duration));
        }

        console.info("Concurrency level per region: %d", concurrencyLevel);

        Link submitLink = traverson.fromRoot()
                .follow(withCurie(ACCOUNT_REL))
                .follow(withCurie(ACCOUNT_BATCH_REL))
                .asLink();

        final AtomicInteger batchNumber = new AtomicInteger();

        IntStream.range(0, concurrencyLevel)
                .forEach(i -> throttledExecutor.submit(() -> {
                    String region = RandomData.selectRandom(regionMap.keySet());

                    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                            .queryParam("region", region)
                            .queryParam("prefix", "batch-" + batchNumber.incrementAndGet())
                            .queryParam("batchSize", batchSize);

                    ResponseEntity<String> response =
                            restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, new HttpEntity<>(null),
                                    String.class);

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        console.warn("Unexpected HTTP status: %s", response.toString());
                    }
                    return null;
                }, taskDuration,
                "create-accounts"
        ));
    }
}
