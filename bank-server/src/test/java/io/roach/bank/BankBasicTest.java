package io.roach.bank;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.test.annotation.Commit;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;

import static io.roach.bank.api.support.Money.SEK;
import static io.roach.bank.api.support.Money.USD;

public class BankBasicTest extends AbstractIntegrationTest {
    @Test
    @Order(2)
    @TransactionBoundary
    @Commit
    public void whenFindingAccounts_expectInitialBalance() {
        Account accountA = accountService.getAccountById(account1Id_SEK);
        Assertions.assertEquals(Money.of("500000.00", SEK), accountA.getBalance());
        Assertions.assertEquals("test-swe-1", accountA.getName());

        Account accountB = accountService.getAccountById(account2Id_SEK);
        Assertions.assertEquals(Money.of("250000.00", SEK), accountB.getBalance());
        Assertions.assertEquals("test-swe-2", accountB.getName());

        Account accountC = accountService.getAccountById(account1Id_USD);
        Assertions.assertEquals(Money.of("500000.00", USD), accountC.getBalance());
        Assertions.assertEquals("test-usa-1", accountC.getName());

        Account accountD = accountService.getAccountById(account2Id_USD);
        Assertions.assertEquals(Money.of("250000.00", USD), accountD.getBalance());
        Assertions.assertEquals("test-usa-2", accountD.getName());
    }

    @Test
    @Order(3)
    @TransactionBoundary
    @Commit
    public void whenSubmitBalancedTransaction_expectSuccess() {
        Account accountA = accountService.getAccountById(account1Id_SEK);
        Account accountB = accountService.getAccountById(account2Id_SEK);

        Assertions.assertNotEquals(accountA, accountB);

        TransactionForm form = TransactionForm.builder()
                .withTransactionType("GEN")
                .withCity("stockholm")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .addLeg()
                .withId(accountA.getId())
                .withAmount(Money.of("-50.00", accountA.getBalance().getCurrency()))
                .withNote("debit A")
                .then()
                .addLeg()
                .withId(accountB.getId())
                .withAmount(Money.of("50.00", accountB.getBalance().getCurrency()))
                .withNote("credit A")
                .then()
                .build();

        Transaction t = transactionService.createTransaction(UUID.randomUUID(), form);
        Assertions.assertNotNull(t);
        Assertions.assertEquals(2, t.getItems().size());
        Assertions.assertEquals("GEN", t.getTransactionType());
        Assertions.assertTrue(t.isNew());
    }

    @Test
    @Order(4)
    @TransactionBoundary
    @Commit
    public void whenSubmitBalancedMultiLeggedTransaction_expectSuccess() {
        Account accountFromSEK = accountService.getAccountById(account1Id_SEK);
        Account accountToSEK = accountService.getAccountById(account2Id_SEK);

        Account accountFromUSD = accountService.getAccountById(account1Id_USD);
        Account accountToUSD = accountService.getAccountById(account2Id_USD);

        Assertions.assertNotEquals(accountFromSEK, accountToSEK);
        Assertions.assertNotEquals(accountFromUSD, accountToUSD);

        TransactionForm request = TransactionForm.builder()
                .withCity("stockholm")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .withTransactionType("GEN")
                .addLeg()
                .withId(accountFromSEK.getId())
                .withAmount(Money.of("-50.00", accountFromSEK.getBalance().getCurrency()))
                .withNote("debit A")
                .then()
                .addLeg()
                .withId(accountFromSEK.getId())
                .withAmount(Money.of("-100.00", accountFromSEK.getBalance().getCurrency()))
                .withNote("debit B")
                .then()
                .addLeg()
                .withId(accountToSEK.getId())
                .withAmount(Money.of("150.00", accountToSEK.getBalance().getCurrency()))
                .withNote("credit AB")
                .then()
                .addLeg()
                .withId(accountFromUSD.getId())
                .withAmount(Money.of("-250.05", accountFromUSD.getBalance().getCurrency()))
                .withNote("debit C")
                .then()
                .addLeg()
                .withId(accountFromUSD.getId())
                .withAmount(Money.of("-0.50", accountFromUSD.getBalance().getCurrency()))
                .withNote("debit D")
                .then()
                .addLeg()
                .withId(accountToUSD.getId())
                .withAmount(Money.of("250.55", accountToUSD.getBalance().getCurrency()))
                .withNote("credit CD")
                .then()
                .build();

        Transaction t = transactionService.createTransaction(UUID.randomUUID(), request);
        Assertions.assertNotNull(t);
        Assertions.assertEquals(4, t.getItems().size());
        Assertions.assertEquals("GEN", t.getTransactionType());
        Assertions.assertTrue(t.isNew());
    }
}
