package io.roach.bank.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class DefaultAccountService implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private Environment environment;

    @Override
    public Account createAccount(Account account) {
        return accountRepository.createAccount(account);
    }

    @Override
    public List<UUID> createAccountBatch(Supplier<Account> factory, int batchSize) {
        return accountRepository.createAccounts(factory, batchSize);
    }

    @Override
    public List<Account> findAccountsByCity(Set<String> cities, int limit) {
        return accountRepository.findByCity(cities, limit);
    }

    @Override
    public Page<Account> findAccountsByCity(Set<String> cities, Pageable page) {
        return accountRepository.findByCity(cities, page);
    }

    @Override
    public Account getAccountById(UUID id) {
        return accountRepository.getAccountById(id)
                .orElseThrow(() -> new NoSuchAccountException(id));
    }

    @Override
    public Money getBalance(UUID id) {
        return accountRepository.getBalance(id);
    }

    @Override
    public Money getBalanceSnapshot(UUID id) {
        if (ProfileNames.acceptsPostgresSQL(environment)) {
            return getBalance(id);
        }
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
