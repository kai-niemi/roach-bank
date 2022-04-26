package io.roach.bank.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

public interface AccountService {
    List<Account> findTopAccountsByCity(String city, int limit);

    Page<Account> findAccountsByCity(Set<String> cities, Pageable page);

    Account getAccountById(UUID id);

    Money getBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    Account openAccount(UUID id);

    Account closeAccount(UUID id);

    void deleteAll();
}
