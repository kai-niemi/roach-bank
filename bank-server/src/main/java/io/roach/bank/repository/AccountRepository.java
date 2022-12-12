package io.roach.bank.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.util.Pair;

public interface AccountRepository {
    void createAccounts(Supplier<Account> factory, int numAccounts, int batchSize);

    Account createAccount(Account account);

    Account getAccountByReference(UUID id);

    Optional<Account> getAccountById(UUID id);

    Money getAccountBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    void closeAccount(UUID id);

    void openAccount(UUID id);

    void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates);

    Page<Account> findAccountsByCity(Set<String> cities, Pageable page);

    List<Account> findTopAccountsByCity(String city, int limit);

    List<Account> findAccountsById(Set<UUID> ids, boolean locking);

    void deleteAll();
}
