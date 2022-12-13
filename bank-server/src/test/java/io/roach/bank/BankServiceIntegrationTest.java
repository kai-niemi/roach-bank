package io.roach.bank;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    private List<Account> accountsSwe;

    private List<Account> accountsUsa;

    @Test
    @Order(1)
    public void whenFindByIdWithRandomID_thenReturnNothing() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        bankService.findById(UUID.randomUUID());
    }

    @Test
    @Order(1)
    public void whenFindingTopAccounts_thenReturnTestAccounts() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        accountsSwe = accountService.findByCity("stockholm", 10);
        accountsUsa = accountService.findByCity("new york", 10);

        Assertions.assertTrue(accountsSwe.size() >= 10, "expected >= 10");
        Assertions.assertTrue(accountsUsa.size() >= 10, "expected >= 10");
    }

    @Test
    @Commit
    @Order(2)
    public void withBalancedTransaction_thenSucceed() {
        Account accountFrom1 = accountsSwe.get(0);
        Account accountTo1 = accountsSwe.get(1);

        Assertions.assertNotEquals(accountFrom1, accountTo1);

        TransactionForm request = TransactionForm.builder()
                .withTransactionType("GEN")
                .withCity("stockholm")
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

        Transaction t = bankService.createTransaction(UUID.randomUUID(), request);
        Assertions.assertNotNull(t);
    }

    @Test
    @Commit
    @Order(3)
    public void withBalancedMultiLeggedTransaction_thenSucceed() {
        Assertions.assertTrue(accountsSwe.size() > 1);
        Assertions.assertTrue(accountsUsa.size() > 1);

        Account accountFrom1 = accountsSwe.get(0);
        Account accountTo1 = accountsSwe.get(1);

        Account accountFrom2 = accountsUsa.get(0);
        Account accountTo2 = accountsUsa.get(1);

        Assertions.assertNotEquals(accountFrom1, accountTo1);
        Assertions.assertNotEquals(accountFrom2, accountTo2);

        TransactionForm request = TransactionForm.builder()
                .withCity("stockholm")
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
    }
}
