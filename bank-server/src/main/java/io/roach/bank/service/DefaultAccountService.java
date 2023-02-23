package io.roach.bank.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.aspect.TimeTravelMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Service
@TransactionBoundary
public class DefaultAccountService implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public Page<Account> findAccountsByCity(Set<String> cities, Pageable page) {
        return accountRepository.findPageByCity(cities, page);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public List<Account> findTopAccountsByCity(String city, int limit) {
        return accountRepository.findByCity(city, limit);
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
        return accountRepository.getBalance(id);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public Money getBalanceSnapshot(UUID id) {
        return accountRepository.getBalanceSnapshot(id);
    }

    @Override
    public Account openAccount(UUID id) {
        accountRepository.openAccount(id);
        return accountRepository.getAccountById(id)
                .orElseThrow(() -> new NoSuchAccountException(id));
    }

    @Override
    public Account closeAccount(UUID id) {
        accountRepository.closeAccount(id);
        return accountRepository.getAccountById(id)
                .orElseThrow(() -> new NoSuchAccountException(id));
    }

    @Override
    public void deleteAll() {
        accountRepository.deleteAll();
    }
}
