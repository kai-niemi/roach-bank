package io.roach.bank.client;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.ExecutorTemplate;
import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.TRANSACTION_FORM_REL;
import static io.roach.bank.api.LinkRelations.TRANSACTION_REL;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Transfer extends AbstractCommand {
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private static final List<String> QUOTES = Arrays.asList(
            "Cockroaches can eat anything",
            "Roaches can live up to a week without their head",
            "There are more than 4,000 species of cockroaches worldwide",
            "Cockroaches can run up to three miles in an hour"
    );

    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    // transfer --limit --amount 150.00 --locking
    @ShellMethod(value = "Transfer funds between accounts", key = {"t", "transfer"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void transfer(
            @ShellOption(help = "min amount", defaultValue = "0.50") final String minAmount,
            @ShellOption(help = "max amount", defaultValue = "10.00") final String maxAmount,
            @ShellOption(help = "number of legs per transaction", defaultValue = "2") final int legs,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) int limit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "execution iterations (precedence over duration if >0)", defaultValue = "0")
                    int iterations,
            @ShellOption(help = "smoke test (do nothing server side)", defaultValue = "false") boolean smokeTest
    ) {
        Map<String, List<AccountModel>> accounts = restCommands.getTopAccounts(
                StringUtils.commaDelimitedListToSet(regions), limit);
        if (accounts.isEmpty()) {
            logger.warn("No cities found matching region(s): {}", regions);
        }

        accounts.forEach((city, accountModels) -> {
            logger.info("Found {} accounts in '{}'", accountModels.size(), city);
        });

        final Link transferLink = restCommands.fromRoot()
                .follow(LinkRelations.withCurie(TRANSACTION_REL))
                .follow(LinkRelations.withCurie(TRANSACTION_FORM_REL))
                .asTemplatedLink();

        double min = Double.parseDouble(minAmount);
        double max = Double.parseDouble(maxAmount);

        accounts.forEach((city, accountModels) -> {
            if (iterations > 0) {
                executorTemplate.runAsync(city + " (transfer) " + iterations,
                        () -> transferFunds(transferLink, city, accountModels, min, max, legs, smokeTest),
                        iterations
                );
            } else {
                executorTemplate.runAsync(city + " (transfer) " + duration,
                        () -> transferFunds(transferLink, city, accountModels, min, max, legs, smokeTest),
                        DurationFormat.parseDuration(duration)
                );
            }
        });
    }

    private void transferFunds(Link link,
                               String city,
                               List<AccountModel> accounts,
                               double minAmount,
                               double maxAmount,
                               int legs,
                               boolean smokeTest) {
        Money transferAmount = RandomData
                .randomMoneyBetween(minAmount, maxAmount, accounts.get(0).getBalance().getCurrency());

        TransactionForm.Builder builder = TransactionForm.builder()
                .withUUID(UUID.randomUUID())
                .withCity(city)
                .withTransactionType("GEN")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now());

        if (smokeTest) {
            builder.withSmokeTest();
        }

        Set<UUID> trail = new HashSet<>();

        IntStream.range(0, legs).forEach(value -> {
            AccountModel account = RandomData.selectRandom(accounts);
            while (trail.contains(account.getId())) {
                account = RandomData.selectRandom(accounts);
            }
            trail.add(account.getId());

            builder.addLeg()
                    .withId(account.getId())
                    .withAmount(value % 2 == 0 ? transferAmount.negate() : transferAmount)
                    .withNote(RandomData.selectRandom(QUOTES))
                    .then();
        });

        restCommands.post(link, builder.build(), TransactionModel.class);
    }
}
