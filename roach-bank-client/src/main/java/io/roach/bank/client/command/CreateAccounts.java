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

import io.roach.bank.api.AccountForm;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.util.TimeFormat;

import static io.roach.bank.api.BankLinkRelations.*;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class CreateAccounts extends RestCommandSupport {
    @ShellMethod(value = "Create accounts", key = {"create"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void createAccounts(
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency,
            @ShellOption(help = "batch size (if > 0)", defaultValue = "-1") int batchSize
    ) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            return;
        }

        final int concurrencyLevel = concurrency > 0 ? concurrency :
                Math.max(1, Runtime.getRuntime().availableProcessors() * 2 / regionMap.size());

        console.info("Account regions: %s", regionMap.keySet());
        console.info("Concurrency level per region: %d", concurrencyLevel);
        console.info("Batch size: %d", batchSize);

        if (batchSize <= 0) {
            Link submitLink = traverson.fromRoot()
                    .follow(withCurie(ACCOUNT_REL))
                    .follow(withCurie(ACCOUNT_FORM_REL))
                    .asTemplatedLink();

            IntStream.range(0, concurrencyLevel).forEach(i -> throttledExecutor.submit(() -> {
                String region = RandomData.selectRandom(regionMap.keySet());
                Currency currency = regionMap.get(region);

                AccountForm form = new AccountForm();
                form.setUuid("auto");
                form.setRegion(region);
                form.setName(RandomData.randomString(12));
                form.setDescription(CockroachFacts.nextFact(256));
                form.setCurrencyCode(currency.getCurrencyCode());
                form.setAccountType(AccountType.EXPENSE);

                return restTemplate.postForLocation(submitLink.getTemplate().expand(), form);
            }, TimeFormat.parseDuration(duration), "create-accounts"));
        } else {
            Link submitLink = traverson.fromRoot()
                    .follow(withCurie(ACCOUNT_REL))
                    .follow(withCurie(ACCOUNT_BATCH_REL))
                    .asLink();

            final AtomicInteger batchNumber = new AtomicInteger();

            IntStream.range(0, concurrencyLevel).forEach(i -> throttledExecutor.submit(() -> {
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
            }, TimeFormat.parseDuration(duration), "create-accounts-batch"));
        }
    }
}
