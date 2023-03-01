package io.roach.bank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.util.ConcurrencyUtils;

import static io.roach.bank.api.support.Money.SEK;

public class BankStressTest extends AbstractIntegrationTest {
    private static final int TOTAL_TRANSACTIONS = 5_000;

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() * 4;

    private final Map<UUID, Money> accounts = new HashMap<>();

    private final List<UUID> transactionIds = Collections.synchronizedList(new ArrayList<>());

    @Test
    @Order(1)
    public void whenReadingBalance_expectEnoughFunds() {
        Assertions.assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        IntStream.rangeClosed(1, 10).forEach(value -> {
            Account account = accountServiceFacade.createAccount(
                    Account.builder()
                            .withGeneratedId()
                            .withCity("stockholm")
                            .withName("test-swe-" + value)
                            .withBalance(Money.of("500000.00", SEK)).withAccountType(AccountType.ASSET)
                            .withUpdated(LocalDateTime.now())
                            .build());
            accounts.put(account.getId(), account.getBalance());

            Assertions.assertTrue(
                    account.getBalance().isGreaterThan(Money.of(TOTAL_TRANSACTIONS + ".00", SEK)));
        });
    }

    @Test
    @Order(2)
    public void whenRunningConcurrently_expectNoErrors() {
        Assertions.assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        Callable<Transaction> callable = () -> {
            TransactionForm.Builder formBuilder = TransactionForm.builder()
                    .withTransactionType("GEN")
                    .withCity("stockholm")
                    .withBookingDate(LocalDate.now())
                    .withTransferDate(LocalDate.now());

            UUID fromId = RandomData.selectRandom(accounts.keySet());
            UUID toId;
            do {
                toId = RandomData.selectRandom(accounts.keySet());
            } while (fromId.equals(toId));

            Money amount = Money.of("1.00", SEK);

            formBuilder
                    .addLeg()
                    .withId(toId)
                    .withAmount(amount)
                    .withNote(CockroachFacts.nextFact())
                    .then()
                    .addLeg()
                    .withId(fromId)
                    .withAmount(amount.negate())
                    .withNote(CockroachFacts.nextFact())
                    .then();

            return transactionServiceFacade.createTransaction(UUID.randomUUID(), formBuilder.build());
        };

        ConcurrencyUtils.runConcurrentlyAndWait(
                NUM_THREADS,
                TOTAL_TRANSACTIONS,
                () -> callable,
                transaction -> transactionIds.add(transaction.getId()),
                throwable -> {
                    Assertions.fail(throwable.getCause());
                    return null;
                });
    }

    @Test
    @Transactional
    @Commit
    @Order(3)
    public void whenWrappingTest_expectConsistentOutcome() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        Money origSum = Money.zero(SEK);
        for (Money origBalance : accounts.values()) {
            origSum = origSum.plus(origBalance);
        }

        Money sum = Money.zero(SEK);
        for (UUID id : accounts.keySet()) {
            Money currentBalance = accountService.getBalance(id);
            sum = sum.plus(currentBalance);
            Assertions.assertFalse(currentBalance.isNegative(), id + " has negative balance!?");
        }

        Assertions.assertEquals(sum, origSum);
        Assertions.assertEquals(TOTAL_TRANSACTIONS, transactionIds.size());

        transactionIds.forEach(id -> {
            Transaction t = transactionService.findById(id);
            Assertions.assertEquals(2, t.getItems().size());
            Assertions.assertEquals("GEN", t.getTransactionType());
            Assertions.assertEquals("stockholm", t.getCity());
            t.getItems().forEach(transactionItem -> {
                if (transactionItem.getAmount().isNegative()) {
                    Assertions.assertEquals(Money.of("-1.00", SEK), transactionItem.getAmount());
                } else {
                    Assertions.assertEquals(Money.of("1.00", SEK), transactionItem.getAmount());
                }
// Varies with config
//                Assertions.assertTrue(transactionItem.getRunningBalance().isGreaterThan(Money.zero(SEK)));
                Assertions.assertEquals("stockholm", transactionItem.getCity());
            });
        });
    }
}
