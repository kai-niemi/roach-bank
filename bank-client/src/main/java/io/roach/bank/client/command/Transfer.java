package io.roach.bank.client.command;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.command.support.DurationFormat;
import io.roach.bank.client.command.support.ExecutorTemplate;
import io.roach.bank.client.command.support.HypermediaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.roach.bank.api.LinkRelations.TRANSFER_FORM_REL;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Transfer extends AbstractCommand {
    @Autowired
    private HypermediaClient bankClient;

    @Autowired
    private ExecutorTemplate executorTemplate;

    @ShellMethod(value = "Transfer funds between accounts", key = {"t", "transfer"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void transfer(
            @ShellOption(help = "amount range (min/max)",
                    defaultValue = "0.50-15.00") final String amount,
            @ShellOption(help = "number of legs per transaction",
                    defaultValue = "2") final int legs,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP,
                    defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) int limit,
            @ShellOption(help = Constants.REGIONS_HELP,
                    defaultValue = Constants.DEFAULT_REGION,
                    valueProvider = RegionProvider.class) String region,
            @ShellOption(help = Constants.DURATION_HELP,
                    defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "execution iterations (precedence over duration if >0)",
                    defaultValue = "0") int iterations,
            @ShellOption(help = "update running balance on accounts (more contention)",
                    defaultValue = "false") boolean updateRunningBalance
    ) {
        if (legs % 2 != 0) {
            console.warn("Account legs must be a multiple of 2");
            return;
        }

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("region", region);

        final Link transferLink = bankClient.fromRoot()
                .follow(LinkRelations.withCurie(TRANSFER_FORM_REL))
                .withTemplateParameters(parameters)
                .asTemplatedLink();

        Map<String, List<AccountModel>> accounts = bankClient.getTopAccounts(region, limit);
        accounts.forEach((city, accountModels) -> {
            logger.info("Found {} accounts in city [{}]", accountModels.size(), city);
            console.success(ListAccounts.printContentTable(accountModels));
        });

        accounts.forEach((city, accountModels) -> {
            if (iterations > 0) {
                executorTemplate.runAsync(region + " (" + city + ")",
                        () -> transferFunds(transferLink, city, accountModels, amount, legs, updateRunningBalance),
                        iterations
                );
            } else {
                executorTemplate.runAsync(region + " (" + city + ")",
                        () -> transferFunds(transferLink, city, accountModels, amount, legs, updateRunningBalance),
                        DurationFormat.parseDuration(duration)
                );
            }
        });
    }

//    private static <T> Stream<List<T>> chunkedStream(Stream<T> stream, int chunkSize) {
//        AtomicInteger idx = new AtomicInteger();
//        return stream.collect(Collectors.groupingBy(x -> idx.getAndIncrement() / chunkSize))
//                .values().stream();
//    }

    private void transferFunds(Link link,
                               String city,
                               List<AccountModel> accounts,
                               String amount,
                               int legs,
                               boolean updateRunningBalance) {
        String[] parts = amount.split("-");
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

        if (updateRunningBalance) {
            builder.withUpdateRunningBalance();
        }

        AtomicInteger c = new AtomicInteger();
        RandomData.selectRandomUnique(accounts, legs)
                .forEach(accountModel -> builder.addLeg()
                        .withId(accountModel.getId())
                        .withAmount(c.incrementAndGet() % 2 == 0 ? transferAmount.negate() : transferAmount)
                        .withNote(CockroachFacts.nextFact())
                        .then());

        bankClient.post(link, builder.build(), TransactionModel.class);
    }
}
