package io.roach.bank.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.cockroachdb.jdbc.spring.annotations.TimeTravel;
import io.cockroachdb.jdbc.spring.annotations.TransactionBoundary;
import io.cockroachdb.jdbc.spring.aspect.TimeTravelMode;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Service
public class DefaultAccountService implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public List<Account> findAccountsById(Set<UUID> ids) {
        return accountRepository.findAccountsById(ids, false);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public Page<Account> findAccountsByCity(Set<String> cities, Pageable page) {
        return accountRepository.findAccountsByCity(cities, page);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public List<Account> findTopAccountsByCity(String city, int limit) {
        return accountRepository.findTopAccountsByCity(city, limit);
    }

    @Override
    @TransactionBoundary(readOnly = true)
    public Account getAccountById(UUID id) {
        return accountRepository.getAccountById(id)
                .orElseThrow(() -> new NoSuchAccountException(id));
    }

    @Override
    @TransactionBoundary(readOnly = true)
    public Money getBalance(UUID id) {
        return accountRepository.getAccountBalance(id);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public Money getBalanceSnapshot(UUID id) {
        return accountRepository.getBalanceSnapshot(id);
    }

    @Override
    @TransactionBoundary
    public Account openAccount(UUID id) {
        accountRepository.openAccount(id);
        return accountRepository.getAccountById(id)
                .orElseThrow(() -> new NoSuchAccountException(id));
    }

    @Override
    @TransactionBoundary
    public Account closeAccount(UUID id) {
        accountRepository.closeAccount(id);
        return accountRepository.getAccountById(id)
                .orElseThrow(() -> new NoSuchAccountException(id));
    }

    @Override
    @TransactionBoundary
    public void deleteAll() {
        accountRepository.deleteAll();
    }
}
