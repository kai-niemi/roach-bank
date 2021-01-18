package io.roach.bank.repository.jpa;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.AccountType;
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
    public void createAccountBatch(String region, Currency currency, NamingStrategy namingStrategy, int batchSize) {
        final Money balance = Money.of("0.00", currency);

        for (int i = 0; i < batchSize; i++) {
            accountRepository.save(Account.builder()
                    .withId(UUID.randomUUID(), region)
                    .withName(namingStrategy.accountName(i))
                    .withBalance(balance)
                    .withAccountType(AccountType.ASSET)
                    .build());
        }

        accountRepository.flush();
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
    public void closeAccount(Account.Id id) {
        Account account = accountRepository.getOne(id);
        if (!account.isClosed()) {
            account.setClosed(true);
        }
    }

    @Override
    public void openAccount(Account.Id id) {
        Account account = accountRepository.getOne(id);
        if (account.isClosed()) {
            account.setClosed(false);
        }
    }

    @Override
    public Account getAccountById(Account.Id id) {
        return accountRepository.getOne(id);
    }

    @Override
    public Money getBalance(Account.Id id) {
        return accountRepository.findBalanceById(id);
    }

    @Override
    public List<Account> findAccountsForUpdate(Set<Account.Id> ids) {
        return accountRepository.findAll(
                ids.stream().map(Account.Id::getUUID).collect(Collectors.toSet()),
                ids.stream().map(Account.Id::getRegion).collect(Collectors.toSet()));
    }

    @Override
    public Page<Account> findAccountPage(Set<String> regions, Pageable page) {
        return accountRepository.findAll(page, new ArrayList<>(regions));
    }

    @Override
    public List<Account> findAccountsByRegion(String region, int limit) {
        return entityManager.createQuery("SELECT a FROM Account a WHERE a.id.region=?1",
                Account.class)
                .setParameter(1, region)
                .setMaxResults(limit)
                .getResultList();
    }
}
