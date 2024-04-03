package io.roach.bank.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.AsyncHelper;
import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.HypermediaClient;

import static io.roach.bank.api.LinkRelations.TRANSFER_FORM_REL;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Transfer extends AbstractCommand {
    @Autowired
    private HypermediaClient bankClient;

    @Autowired
    private AsyncHelper asyncHelper;

    @ShellMethod(value = "Transfer funds between accounts", key = {"t", "transfer"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void transfer(
            @ShellOption(help = "amount range (min/max)",
                    defaultValue = "0.25-7.50") final String amount,
            @ShellOption(help = "number of legs per transaction",
                    defaultValue = "2") final int legs,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP,
                    defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) int limit,
            @ShellOption(help = Constants.REGIONS_HELP,
                    defaultValue = Constants.DEFAULT_REGION,
                    valueProvider = RegionProvider.class) String region,
            @ShellOption(help = "Filter selection for one specific city (must be in regions)",
                    defaultValue = ShellOption.NULL,
                    valueProvider = RegionProvider.class) String city,
            @ShellOption(help = Constants.DURATION_HELP,
                    defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "execution iterations (precedence over duration if >0)",
                    defaultValue = "0") int iterations
    ) {
        if (legs % 2 != 0) {
            console.warn("Account legs must be a multiple of 2");
            return;
        }

        final Link transferLink = bankClient.fromRoot()
                .follow(LinkRelations.withCurie(TRANSFER_FORM_REL))
                .asTemplatedLink();

        logger.info("Find max %d accounts per city in %s".formatted(limit, region));

        Map<String, List<AccountModel>> top = bankClient.getTopAccounts(region, limit);

        if (city != null) {
            if (!top.containsKey(city)) {
                console.warn("City not found in selected regions");
                return;
            }

            List<AccountModel> accountModels = top.get(city);

            logger.info("Found %d accounts in %s".formatted(accountModels.size(), city));

            if (iterations > 0) {
                asyncHelper.runAsync(city + " (" + region + ")",
                        () -> transferFunds(transferLink, city, accountModels, amount, legs),
                        iterations
                );
            } else {
                asyncHelper.runAsync(city + " (" + region + ")",
                        () -> transferFunds(transferLink, city, accountModels, amount, legs),
                        DurationFormat.parseDuration(duration)
                );
            }
        } else {
            top.forEach((c, accountModels) -> {
                logger.info("Found %d accounts in %s".formatted(accountModels.size(), c));

                if (iterations > 0) {
                    asyncHelper.runAsync(c + " (" + region + ")",
                            () -> transferFunds(transferLink, c, accountModels, amount, legs),
                            iterations
                    );
                } else {
                    asyncHelper.runAsync(c + " (" + region + ")",
                            () -> transferFunds(transferLink, c, accountModels, amount, legs),
                            DurationFormat.parseDuration(duration)
                    );
                }
            });
        }
    }

    private void transferFunds(Link link,
                               String city,
                               List<AccountModel> accounts,
                               String amount,
                               int legs) {
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
