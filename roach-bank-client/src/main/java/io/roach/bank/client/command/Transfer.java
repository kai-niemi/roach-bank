package io.roach.bank.client.command;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.SchedulingHelper;
import io.roach.bank.client.util.TimeFormat;

import static io.roach.bank.api.BankLinkRelations.*;
import static io.roach.bank.api.support.RandomData.selectRandom;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class Transfer extends RestCommandSupport {
    protected final Logger transactionLogger = LoggerFactory.getLogger("io.roach.TX_LOG");

    @Autowired
    private SchedulingHelper scheduler;

    // 64 chars
    private static final List<String> QUOTES = Arrays.asList(
            "Cockroaches can eat anything",
            "Roaches can live up to a week without their head",
            "There are more than 4,000 species of cockroaches worldwide",
            "Cockroaches can run up to three miles in an hour"
    );

    @ShellMethod(value = "Transfer funds between accounts", key = {"tf","transfer"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void transfer(
            @ShellOption(help = "amount per transaction (from-to)", defaultValue = "0.25-5.00") final String amount,
            @ShellOption(help = "number of legs per transaction", defaultValue = "2") final int legs,
            @ShellOption(help = "transfer funds across regions using multi-currency transactions", defaultValue = "false") final boolean crossRegion,
            @ShellOption(help = "account balance refresh interval for weighted distribution", defaultValue = "60s") final String refreshInterval,
            @ShellOption(help = Constants.ACCOUNT_LIMIT_HELP, defaultValue = Constants.DEFAULT_ACCOUNT_LIMIT) final int accountLimit,
            @ShellOption(help = Constants.REGIONS_HELP, defaultValue = Constants.EMPTY) String regions,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) final String duration,
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency,
            @ShellOption(help = "enable verbose logging", defaultValue = "false") boolean enableLogging
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

        final long refreshIntervalSec = TimeFormat.parseDuration(refreshInterval).getSeconds();

        final Optional<ScheduledFuture<?>> balanceUpdater = refreshIntervalSec > 0
                ? Optional.of(scheduler.scheduleAtFixedRate(() ->
                updateBalanceSnapshots(accountMap), refreshIntervalSec, refreshIntervalSec))
                : Optional.empty();

        final Link transferLink = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(TRANSACTION_REL))
                .follow(BankLinkRelations.withCurie(TRANSACTION_FORM_REL))
                .asTemplatedLink();

        final String[] amountParts = amount.split("-");

        accountMap.keySet().forEach((regionKey) -> IntStream.range(0, concurrencyLevel)
                .forEach(i -> throttledExecutor.submit(() -> {
                            final Map<String, List<AccountModel>> subjectAccountMap = new HashMap<>();

                            if (crossRegion) {
                                Assert.isTrue(accountMap.size() >= legs, "Not enough accounts");

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
                        TimeFormat.parseDuration(duration),
                        regionKey, // region name
                        groupName -> balanceUpdater.ifPresent(future -> future.cancel(true)))
                ));

        console.info("Account regions: %s", regionMap.keySet());
        console.info("Multi-region: %s", crossRegion);
        console.info("Amount range per transaction: %s", amount);
        console.info("Legs per transaction: %s", legs);
        console.info("Max accounts per region: %d", accountLimit);
        console.info("Refresh balance interval: %ds", refreshIntervalSec);
        console.info("Concurrency level per region: %d", concurrencyLevel);
        console.info("Execution duration: %s", duration);
    }

    private void updateBalanceSnapshots(Map<String, List<AccountModel>> accounts) {
        accounts.forEach((region, accountModels) -> accountModels.forEach(accountModel -> {
            try {
                Link selfLink = accountModel.getLink(withCurie(ACCOUNT_BALANCE_SNAPSHOT_REL)).get();
                Money copy = restTemplate.getForObject(selfLink.toUri(), Money.class);
                accountModel.setBalance(copy);
            } catch (RestClientException e) {
                transactionLogger.trace("Error retrieving account balance snapshot: {}", e.toString());
            }
        }));
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

        ResponseEntity<TransactionModel> response = restTemplate.postForEntity(
                transferLink.getTemplate().expand(),
                transactionForm, TransactionModel.class);

        URI location = response.getHeaders().getLocation();

        if (enableLogging && transactionLogger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            AtomicInteger legC = new AtomicInteger();

            transactionForm.getAccountLegs().forEach(leg -> {
                sb.append(String.format("\n\t(%d) account ID: %s, region: %s, amount: %s", legC.incrementAndGet(),
                        leg.getId(),
                        leg.getRegion(),
                        leg.getAmount()));
            });

            if (response.getStatusCode().is2xxSuccessful()) {
                transactionLogger.debug("{} {} {}",
                        response.getStatusCode().toString(),
                        location,
                        sb.toString());
            } else {
                transactionLogger.debug("{} {} {}",
                        response.getStatusCode().toString(),
                        location,
                        sb.toString());
            }
        }

        return response.getBody();
    }
}
