package io.roach.bank.client;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class Transfer extends CommandSupport {
    @Autowired
    private RestCommands restCommands;

    @Autowired
    private ExecutorTemplate executorTemplate;

    // 64 chars
    private static final List<String> QUOTES = Arrays.asList(
            "Cockroaches can eat anything",
            "Roaches can live up to a week without their head",
            "There are more than 4,000 species of cockroaches worldwide",
            "Cockroaches can run up to three miles in an hour"
    );

    @ShellMethod(value = "Transfer funds between accounts", key = {"t", "transfer"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void transfer(
            @ShellOption(help = "amount per transaction (from-to)", defaultValue = "0.15-1.75") final String amount,
            @ShellOption(help = "number of legs per transaction", defaultValue = "2") final int legs,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) int accountLimit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.CITIES_HELP, defaultValue = Constants.EMPTY) String cities,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = "use locking (select for update)", defaultValue = "false") boolean sfu,
            @ShellOption(help = "fake transfers", defaultValue = "false") boolean fake
    ) {
        final Set<String> cityNames = new HashSet<>();
        cityNames.addAll(restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)));
        cityNames.addAll(StringUtils.commaDelimitedListToSet(cities));

        Map<String, List<AccountModel>> accounts = restCommands.getTopAccounts(cityNames, accountLimit);
        if (accounts.isEmpty()) {
            logger.warn("No cities found matching: {}", cityNames);
        }

        if (fake) {
            accounts.forEach((city, accountModels) -> {
                executorTemplate.runAsync(city,
                        () -> transferFake(), DurationFormat.parseDuration(duration));
            });
        } else {
            final Link transferLink = restCommands.fromRoot()
                    .follow(LinkRelations.withCurie(TRANSACTION_REL))
                    .follow(LinkRelations.withCurie(TRANSACTION_FORM_REL))
                    .asTemplatedLink();

            accounts.forEach((city, accountModels) -> {
                executorTemplate.runAsync(city,
                        () -> transferFunds(transferLink, city, accountModels, amount, legs, sfu),
                        DurationFormat.parseDuration(duration));
            });
        }
    }

    private TransactionModel transferFake() {
        try {
            if (Math.random() > 0.9) {
                Thread.sleep(10_000);
            } else {
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
        }
        return null;
    }

    private TransactionModel transferFunds(Link transferLink,
                                           String city,
                                           List<AccountModel> accounts,
                                           String amount,
                                           int legs,
                                           boolean sfu) {
        Currency currency = accounts.get(0).getBalance().getCurrency();
        String[] amountParts = amount.split("-");
        Money transferAmount = RandomData.randomMoneyBetween(amountParts[0], amountParts[1], currency);

        TransactionForm.Builder transactionFormBuilder = TransactionForm.builder()
                .withUUID(UUID.randomUUID())
                .withCity(city)
                .withTransactionType("GEN")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now());

        if (sfu) {
            transactionFormBuilder.withSelectForUpdate();
        }

        Set<UUID> trail = new HashSet<>();

        IntStream.range(0, legs).forEach(value -> {
            AccountModel account = RandomData.selectRandom(accounts);
            while (trail.contains(account.getId())) {
                account = RandomData.selectRandom(accounts);
            }
            trail.add(account.getId());

            transactionFormBuilder
                    .addLeg()
                    .withId(account.getId())

                    .withAmount(value % 2 == 0 ? transferAmount.negate() : transferAmount)
                    .withNote(RandomData.selectRandom(QUOTES))
                    .then();
        });

        HttpHeaders headers = new HttpHeaders();
//        headers.setETag("");
//        headers.setContentType(MediaTypes.HAL_JSON);

        HttpEntity<TransactionForm> request = new HttpEntity<>(transactionFormBuilder.build(), headers);

        return restCommands.post(transferLink, request, TransactionModel.class).getBody();
    }
}