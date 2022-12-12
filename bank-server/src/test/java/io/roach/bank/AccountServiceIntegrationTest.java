package io.roach.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

import static io.roach.bank.api.support.Money.SEK;

public class AccountServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private AccountRepository accountService;

    @Test
    public void testDummy() {
        Assertions.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    @Commit
    public void testCreateAccountPlan() {
        Account account1 = accountService.createAccount(Account.builder()
                .withGeneratedId()
                .withCity("stockholm")
                .withName("test-swe-1")
                .withBalance(Money.of("500000.00", "SEK"))
                .withAccountType(AccountType.ASSET)
                .build());

        Account account2 = accountService.createAccount(Account.builder()
                .withGeneratedId()
                .withCity("stockholm")
                .withName("test-swe-2")
                .withBalance(Money.of("250000.00", "SEK"))
                .withAccountType(AccountType.LIABILITY)
                .build());

        Assertions.assertEquals(account1.getBalance(), Money.of("500000.00", SEK));
        Assertions.assertEquals(account2.getBalance(), Money.of("250000.00", SEK));
    }
}
