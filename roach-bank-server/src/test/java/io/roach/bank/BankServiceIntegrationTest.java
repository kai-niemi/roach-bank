package io.roach.bank;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.service.BankService;

public class BankServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private BankService bankService;

    @Autowired
    private AccountRepository accountService;

    @Test
    @TransactionBoundary
    public void testDummyLookup() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        bankService.findById(Transaction.Id.of(UUID.randomUUID(), "stockholm"));
    }

    @Test
    @TransactionBoundary
    @Commit
    public void testSimpleTransaction() {
        List<Account> accounts = accountService.findAccountsByRegion("stockholm", 10);

        Assertions.assertTrue(accounts.size() > 0);

        Account accountFrom1 = accounts.get(0);
        Account accountTo1 = accounts.get(1);

        Assertions.assertNotEquals(accountFrom1, accountTo1);

        TransactionForm request = TransactionForm.builder()
                .withTransactionType("GEN")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .addLeg()
                .withId(accountFrom1.getUUID(), accountFrom1.getRegion())
                .withAmount(Money.of("-50.00", accountFrom1.getBalance().getCurrency()))
                .withNote("debit A")
                .then()
                .addLeg()
                .withId(accountTo1.getUUID(), accountTo1.getRegion())
                .withAmount(Money.of("50.00", accountTo1.getBalance().getCurrency()))
                .withNote("credit A")
                .then()
                .build();

        Transaction.Id transaction = bankService.createTransaction(
                Transaction.Id.of(UUID.randomUUID(), accountFrom1.getRegion()), request)
                .getId();

        Assertions.assertEquals(accountFrom1.getRegion(), transaction.getRegion());
    }

    @Test
    @TransactionBoundary
    @Commit
    public void testMultiLeggedMultiCurrencyTransaction() {
        // Different regions and currency
        List<Account> accountsSwe = accountService.findAccountsByRegion("stockholm", 10);
        List<Account> accountsUsa = accountService.findAccountsByRegion("new york", 10);

        Assertions.assertTrue(accountsSwe.size() > 1);
        Assertions.assertTrue(accountsUsa.size() > 1);

        Account accountFrom1 = accountsSwe.get(0);
        Account accountTo1 = accountsSwe.get(1);

        Account accountFrom2 = accountsUsa.get(0);
        Account accountTo2 = accountsUsa.get(1);

        Assertions.assertNotEquals(accountFrom1, accountTo1);
        Assertions.assertNotEquals(accountFrom2, accountTo2);

        TransactionForm request = TransactionForm.builder()
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .withTransactionType("GEN")
                .addLeg()
                .withId(accountFrom1.getUUID(), accountFrom1.getRegion())
                .withAmount(Money.of("-50.00", accountFrom1.getBalance().getCurrency()))
                .withNote("debit A")
                .then()
                .addLeg()
                .withId(accountFrom1.getUUID(), accountFrom1.getRegion())
                .withAmount(Money.of("-100.00", accountFrom1.getBalance().getCurrency()))
                .withNote("debit B")
                .then()
                .addLeg()
                .withId(accountTo1.getUUID(), accountTo1.getRegion())
                .withAmount(Money.of("150.00", accountTo1.getBalance().getCurrency()))
                .withNote("credit AB")
                .then()
                .addLeg()
                .withId(accountFrom2.getUUID(), accountFrom2.getRegion())
                .withAmount(Money.of("-250.50", accountFrom2.getBalance().getCurrency()))
                .withNote("debit C")
                .then()
                .addLeg()
                .withId(accountTo2.getUUID(), accountTo2.getRegion())
                .withAmount(Money.of("250.50", accountTo2.getBalance().getCurrency()))
                .withNote("credit C")
                .then()
                .build();

        Transaction t = bankService.createTransaction(Transaction.Id.of("USA"), request);
        Assertions.assertNotNull(t);

        logger.info("Created {}", t.getId());
    }
}
