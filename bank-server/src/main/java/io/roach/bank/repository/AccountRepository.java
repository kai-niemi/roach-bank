package io.roach.bank.repository;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public interface AccountRepository {
    Account createAccount(Account account);

    List<UUID> createAccounts(Supplier<Account> factory, int batchSize);

    Account getAccountReferenceById(UUID id);

    Optional<Account> getAccountById(UUID id);

    Money getBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    void closeAccount(UUID id);

    void openAccount(UUID id);

    void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates, String city);

    void deleteAll();

    List<Account> findTopByCity(Collection<String> cities, int limit);

    List<Account> findByIDs(Set<UUID> ids, String city, boolean forUpdate);

    Page<Account> findAll(Collection<String> cities, Pageable page);
}
