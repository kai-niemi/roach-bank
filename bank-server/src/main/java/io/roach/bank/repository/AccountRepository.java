package io.roach.bank.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

public interface AccountRepository {
    void createAccounts(Supplier<Account> factory, int numAccounts, int batchSize);

    Account createAccount(Account account);

    Optional<Account> getAccountById(UUID id);

    Money getAccountBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    void closeAccount(UUID id);

    void openAccount(UUID id);

    void updateBalances(List<Account> accounts);

    Page<Account> findAccountPage(Set<String> cities, Pageable page);

    List<Account> findAccountsByCity(String city, int limit);

    List<Account> findAccountsById(Set<UUID> ids, boolean sfu);

    void deleteAll();
}
