package io.roach.bank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Transaction;

public class BankStressTest extends AbstractIntegrationTest {
    private static final int TRANSACTIONS_PER_THREAD = 50;

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() * 8;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private UUID fromAccountId;

    private UUID toAccountId;

    private final List<UUID> transactionIds = Collections.synchronizedList(new ArrayList<>());

    private Money fromAccountOrigBalance;

    private Money toAccountOrigBalance;

    @Test
    @Transactional
    @Order(1)
    public void whenReadingBalance_expectEnoughFunds() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        this.fromAccountId = super.account1Id_SEK;
        this.toAccountId = super.account2Id_SEK;

        this.fromAccountOrigBalance = accountService.getBalance(fromAccountId);
        this.toAccountOrigBalance = accountService.getBalance(toAccountId);

        Assertions.assertTrue(
                fromAccountOrigBalance.isGreaterThan(Money.of(TRANSACTIONS_PER_THREAD + ".00", "SEK")));
        Assertions.assertFalse(
                toAccountOrigBalance.isNegative());
    }

    @Test
    @Order(2)
    public void whenRunningConcurrently_expectNoErrors() {
        Assertions.assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        List<Future<Money>> futures = new ArrayList<>();

        IntStream.rangeClosed(1, NUM_THREADS).forEach(value -> {
            Future<Money> future = executorService.submit(() -> {
                Money totalAmount = Money.zero(Money.SEK);

                for (int i = 0; i < TRANSACTIONS_PER_THREAD; i++) {
                    Money amount = Money.of("1.00", "SEK");

                    TransactionForm.Builder formBuilder = TransactionForm.builder()
                            .withTransactionType("GEN")
                            .withCity("stockholm")
                            .withBookingDate(LocalDate.now())
                            .withTransferDate(LocalDate.now());

                    formBuilder
                            .addLeg()
                            .withId(toAccountId)
                            .withAmount(amount)
                            .withNote(CockroachFacts.nextFact())
                            .then()
                            .addLeg()
                            .withId(fromAccountId)
                            .withAmount(amount.negate())
                            .withNote(CockroachFacts.nextFact())
                            .then();

                    Transaction t = transactionServiceFacade.createTransaction(UUID.randomUUID(), formBuilder.build());
                    Assertions.assertNotNull(t.getId());

                    transactionIds.add(t.getId());

                    totalAmount = totalAmount.plus(amount);
                }
                return totalAmount;
            });
            futures.add(future);
        });

        int errors = 0;
        while (!futures.isEmpty()) {
            try {
                Money total = futures.remove(0).get();
                logger.info("Finished: {}", total);
                Assertions.assertEquals(Money.of(TRANSACTIONS_PER_THREAD + ".00", "SEK"), total);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                errors++;
                Assertions.fail(e.getCause());
            }
        }

        executorService.shutdownNow();

        Assertions.assertEquals(0, errors);
    }

    @Test
    @Transactional
    @Commit
    @Order(3)
    public void whenWrappingTest_expectConsistentOutcome() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        Assertions.assertEquals(accountService.getBalance(fromAccountId),
                fromAccountOrigBalance.minus(Money.of(NUM_THREADS * TRANSACTIONS_PER_THREAD + ".00", "SEK")));

        Assertions.assertEquals(accountService.getBalance(toAccountId),
                toAccountOrigBalance.plus(Money.of(NUM_THREADS * TRANSACTIONS_PER_THREAD + ".00", "SEK")));

        Assertions.assertEquals(NUM_THREADS * TRANSACTIONS_PER_THREAD, transactionIds.size());

        transactionIds.forEach(id -> {
            Transaction t = transactionService.findById(id);
            Assertions.assertEquals(2, t.getItems().size());
            Assertions.assertEquals("GEN", t.getTransactionType());
            Assertions.assertEquals("stockholm", t.getCity());
            t.getItems().forEach(transactionItem -> {
                if (transactionItem.getAmount().isNegative()) {
                    Assertions.assertEquals(Money.of("-1.00", "SEK"), transactionItem.getAmount());
                } else {
                    Assertions.assertEquals(Money.of("1.00", "SEK"), transactionItem.getAmount());
                }
                Assertions.assertTrue(transactionItem.getRunningBalance().isGreaterThan(Money.zero("SEK")));
                Assertions.assertEquals("stockholm", transactionItem.getCity());
            });
        });
    }
}
