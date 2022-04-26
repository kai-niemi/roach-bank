package io.roach.bank.repository.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.annotation.TransactionNotAllowed;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Service
@TransactionMandatory
@Profile(ProfileNames.JPA)
public class JpaAccountRepository implements AccountRepository {
    @Autowired
    private AccountJpaRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void createAccounts(Supplier<Account> factory, int numAccounts, int batchSize) {
        IntStream.rangeClosed(1, numAccounts).forEach(value -> {
            if (value > 0 && value % batchSize == 0) {
                accountRepository.flush();
            }
            Account account = factory.get();
            accountRepository.save(account);
        });
    }

    @Override
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public void updateBalances(List<Account> accounts) {
        // No-op, batch update via transparent persistence
    }

    @Override
    public void closeAccount(UUID id) {
        Account account = accountRepository.getById(id);
        if (!account.isClosed()) {
            account.setClosed(true);
        }
    }

    @Override
    public void openAccount(UUID id) {
        Account account = accountRepository.getById(id);
        if (account.isClosed()) {
            account.setClosed(false);
        }
    }

    @Override
    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Money getAccountBalance(UUID id) {
        return accountRepository.findBalanceById(id);
    }

    @Override
    @TransactionNotAllowed
    public Money getBalanceSnapshot(UUID id) {
        Tuple tuple = accountRepository.findBalanceSnapshot(id.toString());
        return Money.of(
                tuple.get(1, BigDecimal.class).toPlainString(),
                tuple.get(0, String.class));
    }

    @Override
    public List<Account> findAccountsById(Set<UUID> ids, String city, boolean sfu) {
        if (sfu) {
            return accountRepository.findAllWithLock(ids);
        }
        return accountRepository.findAll(ids);
    }

    @Override
    public Page<Account> findAccountsByCity(Set<String> cities, Pageable page) {
        return accountRepository.findAll(page, new ArrayList<>(cities));
    }

    @Override
    public List<Account> findTopAccountsByCity(String city, int limit) {
        return entityManager.createQuery("SELECT a FROM Account a WHERE a.id.city=?1",
                        Account.class)
                .setParameter(1, city)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public void deleteAll() {
        accountRepository.deleteAllInBatch();
    }
}
