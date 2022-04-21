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
import io.roach.bank.service.TransactionService;

public class BankServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private TransactionService bankService;

    @Autowired
    private AccountRepository accountService;

    @Test
    @TransactionBoundary
    public void testDummyLookup() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        bankService.findById(UUID.randomUUID());
    }

    @Test
    @TransactionBoundary
    @Commit
    public void testSimpleTransaction() {
        List<Account> accounts = accountService.findAccountsByCity("stockholm", 10);

        Assertions.assertTrue(accounts.size() > 0);

        Account accountFrom1 = accounts.get(0);
        Account accountTo1 = accounts.get(1);

        Assertions.assertNotEquals(accountFrom1, accountTo1);

        TransactionForm request = TransactionForm.builder()
                .withTransactionType("GEN")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .addLeg()
                .withId(accountFrom1.getId())
                .withAmount(Money.of("-50.00", accountFrom1.getBalance().getCurrency()))
                .withNote("debit A")
                .then()
                .addLeg()
                .withId(accountTo1.getId())
                .withAmount(Money.of("50.00", accountTo1.getBalance().getCurrency()))
                .withNote("credit A")
                .then()
                .build();

        UUID transaction = bankService.createTransaction(UUID.randomUUID(), request).getId();
    }

    @Test
    @TransactionBoundary
    @Commit
    public void testMultiLeggedMultiCurrencyTransaction() {
        // Different regions and currency
        List<Account> accountsSwe = accountService.findAccountsByCity("stockholm", 10);
        List<Account> accountsUsa = accountService.findAccountsByCity("new york", 10);

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
                .withId(accountFrom1.getId())
                .withAmount(Money.of("-50.00", accountFrom1.getBalance().getCurrency()))
                .withNote("debit A")
                .then()
                .addLeg()
                .withId(accountFrom1.getId())
                .withAmount(Money.of("-100.00", accountFrom1.getBalance().getCurrency()))
                .withNote("debit B")
                .then()
                .addLeg()
                .withId(accountTo1.getId())
                .withAmount(Money.of("150.00", accountTo1.getBalance().getCurrency()))
                .withNote("credit AB")
                .then()
                .addLeg()
                .withId(accountFrom2.getId())
                .withAmount(Money.of("-250.50", accountFrom2.getBalance().getCurrency()))
                .withNote("debit C")
                .then()
                .addLeg()
                .withId(accountTo2.getId())
                .withAmount(Money.of("250.50", accountTo2.getBalance().getCurrency()))
                .withNote("credit C")
                .then()
                .build();

        Transaction t = bankService.createTransaction(UUID.randomUUID(), request);
        Assertions.assertNotNull(t);

        logger.info("Created {}", t.getId());
    }
}
