package io.roach.bank;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.service.AccountService;
import io.roach.bank.service.AccountServiceFacade;
import io.roach.bank.service.TransactionService;
import io.roach.bank.service.TransactionServiceFacade;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestApplication.class)
@Tag("integration-test")
//@ActiveProfiles({ProfileNames.PGJDBC_DEV, ProfileNames.RETRY_CLIENT, ProfileNames.CDC_NONE, "integrationtest"})
@ActiveProfiles({ProfileNames.PSQL_DEV, ProfileNames.RETRY_CLIENT, ProfileNames.CDC_NONE, "integrationtest"})
public abstract class AbstractIntegrationTest {
    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected TransactionServiceFacade transactionServiceFacade;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected AccountServiceFacade accountServiceFacade;

    protected UUID account1Id_SEK;

    protected UUID account2Id_SEK;

    protected UUID account1Id_USD;

    protected UUID account2Id_USD;

    @BeforeAll
    public void whenCreateAccounts_expectAccountsToBePersisted() {
        Assertions.assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        account1Id_SEK = accountServiceFacade.createAccount(
                Account.builder().withGeneratedId().withCity("stockholm").withName("test-swe-1")
                        .withBalance(Money.of("500000.00", "SEK")).withAccountType(AccountType.ASSET)
                        .withUpdated(LocalDateTime.now()).build()).getId();

        account2Id_SEK = accountServiceFacade.createAccount(
                Account.builder().withGeneratedId().withCity("stockholm").withName("test-swe-2")
                        .withBalance(Money.of("250000.00", "SEK")).withAccountType(AccountType.LIABILITY)
                        .withUpdated(LocalDateTime.now()).build()).getId();

        account1Id_USD = accountServiceFacade.createAccount(
                Account.builder().withGeneratedId().withCity("new york").withName("test-usa-1")
                        .withBalance(Money.of("500000.00", "USD")).withAccountType(AccountType.ASSET)
                        .withUpdated(LocalDateTime.now()).build()).getId();

        account2Id_USD = accountServiceFacade.createAccount(
                Account.builder().withGeneratedId().withCity("new york").withName("test-usa-2")
                        .withBalance(Money.of("250000.00", "USD")).withAccountType(AccountType.LIABILITY)
                        .withUpdated(LocalDateTime.now()).build()).getId();
    }
}
