package io.roach.bank.repository.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
@Profile(ProfileNames.JPA)
public class JpaAccountRepository implements AccountRepository {
    @Autowired
    private AccountJpaRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Environment environment;

    @Override
    public List<UUID> createAccounts(Supplier<Account> factory, int batchSize) {
        List<UUID> ids = new ArrayList<>();
        IntStream.rangeClosed(1, batchSize).forEach(value -> {
            Account account = factory.get();
            accountRepository.save(account);
            ids.add(account.getId());
        });
        return ids;
    }

    @Override
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates) {
        balanceUpdates.forEach(pair -> {
            Query q = entityManager.createQuery("UPDATE Account a"
                    + " SET"
                    + "   a.balance.amount = a.balance.amount + ?2"
                    + " WHERE a.id = ?1"
                    + "   AND a.closed=false"
                    + "   AND (a.balance.amount + ?2) * abs(a.allowNegative-1) >= 0");
            q.setParameter(1, pair.getFirst());
            q.setParameter(2, pair.getSecond());
            int r = q.executeUpdate();
            if (r != 1) {
                throw new IncorrectResultSizeDataAccessException(1, r);
            }
        });
    }

    @Override
    public void closeAccount(UUID id) {
        Account account = accountRepository.getReferenceById(id);
        if (!account.isClosed()) {
            account.setClosed(true);
        }
    }

    @Override
    public void openAccount(UUID id) {
        Account account = accountRepository.getReferenceById(id);
        if (account.isClosed()) {
            account.setClosed(false);
        }
    }

    @Override
    public Account getAccountReferenceById(UUID id) {
        return accountRepository.getReferenceById(id);
    }

    @Override
    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Money getBalance(UUID id) {
        return accountRepository.findBalanceById(id);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Money getBalanceSnapshot(UUID id) {
        if (ProfileNames.acceptsPostgresSQL(environment)) {
           return getBalance(id);
        }
        Tuple tuple = accountRepository.findBalanceSnapshot(id.toString());
        return Money.of(
                tuple.get(1, BigDecimal.class).toPlainString(),
                tuple.get(0, String.class));
    }

    @Override
    public List<Account> findByIDs(Set<UUID> ids, boolean forUpdate) {
        return forUpdate
                ? accountRepository.findAllWithLock(ids)
                : accountRepository.findAll(ids);
    }

    @Override
    public Page<Account> findByCity(Set<String> cities, Pageable page) {
        return accountRepository.findAll(page, new ArrayList<>(cities));
    }

    @Override
    public List<Account> findByCity(Set<String> cities, int limit) {
        // TODO! window function
        List<Account> accounts = new ArrayList<>();
        cities.forEach(city -> {
            accounts.addAll(entityManager.createQuery("SELECT a FROM Account a WHERE a.city=?1",
                            Account.class)
                    .setParameter(1, city)
                    .setMaxResults(limit)
                    .getResultList());
        });
        return accounts;
    }

    @Override
    public void deleteAll() {
        accountRepository.deleteAllInBatch();
    }
}
