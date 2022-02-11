package io.roach.bank.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

public interface AccountService {
    Page<Account> findAccountPage(Collection<String> regions, Pageable page);

    List<Account> findAccountsByRegion(String region, int limit);

    Set<String> resolveRegions(Collection<String> regions);

    Account getAccountById(Account.Id id);

    Money getBalance(Account.Id id);

    Money getBalanceSnapshot(Account.Id id);

    Account openAccount(Account.Id id);

    Account closeAccount(Account.Id id);

    void deleteAll();
}
