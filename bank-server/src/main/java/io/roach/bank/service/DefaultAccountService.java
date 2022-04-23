package io.roach.bank.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Service
public class DefaultAccountService implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Account> findAccountPage(Set<String> cities, Pageable page) {
        return accountRepository.findAccountPage(cities, page);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public List<Account> findAccountsByCity(String city, int limit) {
        return accountRepository.findAccountsByCity(city, limit);
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
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
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
