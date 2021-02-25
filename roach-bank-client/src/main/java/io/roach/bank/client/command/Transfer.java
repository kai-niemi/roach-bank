package io.roach.bank.client.command;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.CountDuration;
import io.roach.bank.client.support.TaskDuration;
import io.roach.bank.client.support.TimeDuration;
import io.roach.bank.client.util.DurationFormat;

import static io.roach.bank.api.BankLinkRelations.TRANSACTION_FORM_REL;
import static io.roach.bank.api.BankLinkRelations.TRANSACTION_REL;
import static io.roach.bank.api.support.RandomData.selectRandom;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class Transfer extends RestCommandSupport {
    protected final Logger transactionLogger = LoggerFactory.getLogger("io.roach.TX_LOG");

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
            @ShellOption(help = "amount per transaction (from-to)", defaultValue = "0.25-5.00") final String amount,
            @ShellOption(help = "number of legs per transaction", defaultValue = "2") final int legs,
            @ShellOption(help = "transfer funds across regions", defaultValue = "false") final boolean crossRegion,
            @ShellOption(help = "account balance refresh interval", defaultValue = "30m") final String refreshInterval,
            @ShellOption(help = "number of transactions rather than duration (if > 0)", defaultValue = "-1") int transactions,
            @ShellOption(help = "enable verbose logging", defaultValue = "false") boolean enableLogging,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) int accountLimit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency
    ) {
        final Map<String, Currency> regionMap = lookupRegions(regions);
        if (regionMap.isEmpty()) {
            return;
        }
        final Map<String, List<AccountModel>> accountMap = lookupAccounts(regionMap.keySet(), accountLimit);
        if (accountMap.isEmpty()) {
            return;
        }

        final int concurrencyLevel = concurrency > 0 ? concurrency :
                Math.max(1, Runtime.getRuntime().availableProcessors() * 2 / regionMap.size());

        final long refreshIntervalSec = DurationFormat.parseDuration(refreshInterval).getSeconds();

        final Link transferLink = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(TRANSACTION_REL))
                .follow(BankLinkRelations.withCurie(TRANSACTION_FORM_REL))
                .asTemplatedLink();

        final String[] amountParts = amount.split("-");

        final TaskDuration taskDuration = transactions > 0
                ? CountDuration.of(transactions)
                : TimeDuration.of(DurationFormat.parseDuration(duration));

        accountMap.keySet().forEach((regionKey) -> IntStream.range(0, concurrencyLevel)
                .forEach(i -> throttledExecutor.submit(() -> {
                            final Map<String, List<AccountModel>> subjectAccountMap = new HashMap<>();

                            if (crossRegion) {
                                IntStream.range(0, legs).forEach(value -> {
                                    while (true) {
                                        String k = RandomData.selectRandom(accountMap.keySet());
                                        if (!subjectAccountMap.containsKey(k)) {
                                            subjectAccountMap.put(k, accountMap.get(k));
                                            break;
                                        }
                                    }
                                });
                            } else {
                                subjectAccountMap.put(regionKey, accountMap.get(regionKey));
                            }

                            return executeOneTransfer(transferLink, regionKey, subjectAccountMap, amountParts, legs,
                                    enableLogging);
                        },
                        taskDuration,
                        regionKey + " transfer")
                ));

        console.info("Multi-region: %s", crossRegion);
        console.info("Amount range per transaction: %s", amount);
        console.info("Legs per transaction: %s", legs);
        console.info("Max accounts per region: %d", accountLimit);
        console.info("Refresh balance interval: %ds", refreshIntervalSec);
        console.info("Concurrency level per region: %d", concurrencyLevel);
        console.info("Execution duration: %s", duration);
    }

    private TransactionModel executeOneTransfer(Link transferLink,
                                                String region,
                                                Map<String, List<AccountModel>> accountMap,
                                                String[] amountParts,
                                                int legs,
                                                boolean enableLogging) {
        TransactionForm.Builder formBuilder = TransactionForm.builder()
                .withUUID("auto")
                .withRegion(region)
                .withTransactionType("GEN")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now());

        final int legCount = legs % 2 != 0 ? ++legs : legs;

        accountMap.forEach((regionKey, accounts) -> {
            List<AccountModel> workingSet = new ArrayList<>(accounts);
            Currency currency = workingSet.get(0).getBalance().getCurrency();

            final Money transferAmount = amountParts.length > 1
                    ? RandomData.randomMoneyBetween(amountParts[0], amountParts[1], currency)
                    : Money.of(amountParts[0], currency);

            IntStream.range(0, legCount).forEach(value -> {
                // Debits gravitate towards accounts with highest balance
                AccountModel account = value % 2 == 0
                        ? RandomData.selectRandomWeighted(workingSet)
                        : selectRandom(workingSet);

                workingSet.remove(account);

                Money amount = value % 2 == 0 ? transferAmount.negate() : transferAmount;

                formBuilder
                        .addLeg()
                        .withId(account.getId(), account.getRegion())
                        .withAmount(amount)
                        .withNote(selectRandom(QUOTES))
                        .then();
            });
        });

        final TransactionForm transactionForm = formBuilder.build();

        long startTime = System.currentTimeMillis();

        ResponseEntity<TransactionModel> response = restTemplate.postForEntity(
                transferLink.getTemplate().expand(),
                transactionForm,
                TransactionModel.class);

        final long rtt = System.currentTimeMillis() - startTime;

        if (enableLogging && transactionLogger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            AtomicInteger legC = new AtomicInteger();

            transactionForm.getAccountLegs().forEach(leg -> {
                sb.append(String.format("\n\t(%d) account ID: %s, region: %s, amount: %s", legC.incrementAndGet(),
                        leg.getId(),
                        leg.getRegion(),
                        leg.getAmount()));
            });

            transactionLogger.debug("{}: transaction ID: {}, region: {}, rtt {}ms {}",
                    response.getStatusCode().toString(),
                    transactionForm.getUuid(),
                    transactionForm.getRegion(),
                    rtt,
                    sb.toString());
        }

        return response.getBody();
    }
}
