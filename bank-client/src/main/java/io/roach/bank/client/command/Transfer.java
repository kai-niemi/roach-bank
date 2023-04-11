package io.roach.bank.client.command;

import java.time.LocalDate;
import java.util.HashMap;
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
import io.roach.bank.client.command.support.BankClient;
import io.roach.bank.client.command.support.DurationFormat;

import static io.roach.bank.api.LinkRelations.TRANSFER_FORM_REL;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Transfer extends AbstractCommand {
    @Autowired
    private BankClient bankClient;

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
                    defaultValue = Constants.EMPTY,
                    valueProvider = RegionProvider.class) String regions,
            @ShellOption(help = Constants.DURATION_HELP,
                    defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "execution iterations (precedence over duration if >0)",
                    defaultValue = "0") int iterations,
            @ShellOption(help = "number of worker threads per city",
                    defaultValue = "1") int concurrency,
            @ShellOption(help = "enable smoke test (pass HTTP requests as no-ops server side)",
                    defaultValue = "false") boolean smokeTest
    ) {
        if (legs % 2 != 0) {
            console.warnf("Account legs must be a multiple of 2");
            return;
        }

        final Set<String> regionSet = StringUtils.commaDelimitedListToSet(regions);

        final Map<String, Object> parameters = new HashMap<>();
        if (regionSet.isEmpty()) {
            regionSet.add(bankClient.getGatewayRegion());
            console.warnf("No region(s) specified - defaulting to gateway region %s", regionSet);
        }
        parameters.put("regions", regionSet);

        Map<String, List<AccountModel>> accounts = bankClient.getTopAccounts(regionSet, limit);
        if (accounts.isEmpty()) {
            logger.warn("No cities found matching region(s): {}", regionSet);
        }

        accounts.forEach((city, accountModels) -> {
            logger.info("Found {} accounts in city '{}'", accountModels.size(), city);
        });

        final Link transferLink = bankClient.fromRoot()
                .follow(LinkRelations.withCurie(TRANSFER_FORM_REL))
                .withTemplateParameters(parameters)
                .asTemplatedLink();

        accounts.forEach((city, accountModels) -> {
            IntStream.rangeClosed(1, concurrency).forEach(value -> {
                if (iterations > 0) {
                    executorTemplate.runAsync(city + " (transfer)",
                            () -> transferFunds(transferLink, city, accountModels, amount, legs, smokeTest),
                            iterations
                    );
                } else {
                    executorTemplate.runAsync(city + " (transfer)",
                            () -> transferFunds(transferLink, city, accountModels, amount, legs, smokeTest),
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
                               boolean smokeTest) {
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
                    .withNote(CockroachFacts.nextFact())
                    .then();
        });

        bankClient.post(link, builder.build(), TransactionModel.class);
    }
}
