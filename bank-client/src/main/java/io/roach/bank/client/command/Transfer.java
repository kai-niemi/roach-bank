package io.roach.bank.client.command;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.util.DurationFormat;

import static io.roach.bank.api.LinkRelations.TRANSACTION_FORM_REL;
import static io.roach.bank.api.LinkRelations.TRANSACTION_REL;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Transfer extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Transfer funds between accounts", key = {"t", "transfer"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void transfer(
            @ShellOption(help = "amount range (min/max)", defaultValue = "5.00-15.00") final String amount,
            @ShellOption(help = "number of legs per transaction", defaultValue = "2") final int legs,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) int limit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY,
                    valueProvider = RegionProvider.class) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "execution iterations (precedence over duration if >0)", defaultValue = "0")
            int iterations,
            @ShellOption(help = "number of threads per city", defaultValue = "1") int concurrency,
            @ShellOption(help = "fake test run", defaultValue = "false") boolean fake
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

        accounts.forEach((city, accountModels) -> {
            IntStream.rangeClosed(1, concurrency).forEach(value -> {
                if (iterations > 0) {
                    executorTemplate.runAsync(city + " (transfer) " + iterations,
                            () -> transferFunds(transferLink, city, accountModels, amount, legs, fake),
                            iterations
                    );
                } else {
                    executorTemplate.runAsync(city + " (transfer) " + duration,
                            () -> transferFunds(transferLink, city, accountModels, amount, legs, fake),
                            DurationFormat.parseDuration(duration)
                    );
                }
            });
        });
    }

    private void transferFunds(Link link,
                               String city,
                               List<AccountModel> accounts,
                               String amount,
                               int legs,
                               boolean fake) {
        String parts[] = amount.split("-");
        String minAmount = parts.length > 1 ? parts[0] : amount;
        String maxAmount = parts.length > 1 ? parts[1] : amount;

        Money transferAmount = RandomData
                .randomMoneyBetween(minAmount, maxAmount, accounts.get(0).getBalance().getCurrency());

        TransactionForm.Builder builder = TransactionForm.builder()
                .withUUID(UUID.randomUUID())
                .withCity(city)
                .withTransactionType("GEN")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now());

        if (fake) {
            builder.withFakeFlag();
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
                    .withNote(CockroachFacts.nextFact())
                    .then();
        });

        restCommands.post(link, builder.build(), TransactionModel.class);
    }
}
