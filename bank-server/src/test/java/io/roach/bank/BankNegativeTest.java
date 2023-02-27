package io.roach.bank;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.test.annotation.Rollback;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.service.BadRequestException;

public class BankNegativeTest extends AbstractIntegrationTest {
    @Test
    @TransactionBoundary
    @Rollback
    @Order(2)
    public void whenNonZeroSumTransfer_expectRequestToFail() {
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
                .withAmount(Money.of("50.05", accountB.getBalance().getCurrency()))
                .withNote("credit A")
                .then()
                .build();

        Assertions.assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(UUID.randomUUID(), form);
        });
    }

    @Test
    @TransactionBoundary
    @Rollback
    @Order(3)
    public void whenMixedCurrencyTransfer_expectRequestToFail() {
        Account accountA = accountService.getAccountById(account1Id_SEK);
        Account accountB = accountService.getAccountById(account1Id_USD);

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
                .withAmount(Money.of("50.00", "USD"))
                .withNote("credit A")
                .then()
                .build();

        Assertions.assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(UUID.randomUUID(), form);
        });
    }
}
