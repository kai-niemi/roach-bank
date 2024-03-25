package io.roach.bank.service;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface AccountService {
    Account createAccount(Account account);

    List<UUID> createAccountBatch(Supplier<Account> factory, int batchSize);

    List<Account> findTopAccountsByCity(Collection<String> cities, int limit);

    Page<Account> findAll(Collection<String> cities, Pageable page);

    Account getAccountById(UUID id);

    Money getBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    Account openAccount(UUID id);

    Account closeAccount(UUID id);

    void deleteAll();
}
