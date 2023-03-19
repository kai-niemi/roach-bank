package io.roach.bank.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

public interface AccountRepository {
    Account createAccount(Account account);

    List<UUID> createAccounts(Supplier<Account> factory, int numAccounts, int batchSize);

    Account getAccountReferenceById(UUID id);

    Optional<Account> getAccountById(UUID id);

    Money getBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    void closeAccount(UUID id);

    void openAccount(UUID id);

    void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates);

    void deleteAll();

    Page<Account> findByCity(Set<String> cities, Pageable page);

    List<Account> findByCity(String city, int limit);

    List<Account> findByIDs(Set<UUID> ids, boolean forUpdate);
}
