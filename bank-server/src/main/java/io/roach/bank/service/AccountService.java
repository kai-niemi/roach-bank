package io.roach.bank.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

public interface AccountService {
    Page<Account> findAccountPage(Set<String> cities, Pageable page);

    List<Account> findAccountsByCity(String city, int limit);

    Account getAccountById(UUID id);

    Money getBalance(UUID id);

    Money getBalanceSnapshot(UUID id);

    Account openAccount(UUID id);

    Account closeAccount(UUID id);

    void deleteAll();
}
