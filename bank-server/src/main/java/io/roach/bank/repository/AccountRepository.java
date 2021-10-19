package io.roach.bank.repository;

import java.util.Currency;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

public interface AccountRepository {
    interface NamingStrategy {
        String accountName(int sequence);
    }

    void createAccountBatch(String region, Currency currency, NamingStrategy namingStrategy, int batchSize);

    Account createAccount(Account account);

    Account getAccountById(Account.Id id);

    Money getBalance(Account.Id id);

    Money getBalanceSnapshot(Account.Id id);

    void closeAccount(Account.Id id);

    void openAccount(Account.Id id);

    void updateBalances(List<Account> accounts);

    Page<Account> findAccountPage(Set<String> regions, Pageable page);

    List<Account> findAccountsByRegion(String region, int limit);

    List<Account> findAccountsForUpdate(Set<Account.Id> ids);

    void deleteAll();
}
