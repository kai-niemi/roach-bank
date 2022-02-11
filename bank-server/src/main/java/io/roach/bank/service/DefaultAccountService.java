package io.roach.bank.service;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.repository.MetadataRepository;

@Service
public class DefaultAccountService implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Account> findAccountPage(Collection<String> regions, Pageable page) {
        Map<String, Currency> resolvedRegions = metadataRepository.resolveRegions(regions);
        return accountRepository.findAccountPage(resolvedRegions.keySet(), page);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public List<Account> findAccountsByRegion(String region, int limit) {
        return accountRepository.findAccountsByRegion(region, limit);
    }

    @Override
    @TransactionBoundary(readOnly = true)
    public Set<String> resolveRegions(Collection<String> regions) {
        return metadataRepository.resolveRegions(regions).keySet();
    }

    @Override
    @TransactionBoundary(readOnly = true)
    public Account getAccountById(Account.Id id) {
        return accountRepository.getAccountById(id);
    }

    @Override
    @TransactionBoundary(readOnly = true)
    public Money getBalance(Account.Id id) {
        return accountRepository.getBalance(id);
    }

    @Override
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Money getBalanceSnapshot(Account.Id id) {
        return accountRepository.getBalanceSnapshot(id);
    }

    @Override
    @TransactionBoundary
    public Account openAccount(Account.Id id) {
        accountRepository.openAccount(id);
        return accountRepository.getAccountById(id);
    }

    @Override
    @TransactionBoundary
    public Account closeAccount(Account.Id id) {
        accountRepository.closeAccount(id);
        return accountRepository.getAccountById(id);
    }

    @Override
    @TransactionBoundary
    public void deleteAll() {
        accountRepository.deleteAll();
    }
}
