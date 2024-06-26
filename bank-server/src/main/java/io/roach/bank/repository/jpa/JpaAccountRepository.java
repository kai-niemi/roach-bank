package io.roach.bank.repository.jpa;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

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
    public void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates, String city) {
        balanceUpdates.forEach(pair -> {
            Query q = entityManager.createQuery("UPDATE Account a"
                    + " SET"
                    + "   a.balance.amount = a.balance.amount + ?2"
                    + " WHERE a.id = ?1"
                    + "   AND a.closed=false"
                    + "   AND a.city=?3"
                    + "   AND (a.balance.amount + ?2) * abs(a.allowNegative-1) >= 0");
            q.setParameter(1, pair.getFirst());
            q.setParameter(2, pair.getSecond());
            q.setParameter(3, city);
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
        Query q = entityManager.createNativeQuery("select a.currency,a.balance " +
                "from account a " +
                "as of system time follower_read_timestamp() " +
                "where a.id = ?1", Tuple.class);
        q.setParameter(1, id);
        Tuple tuple = (Tuple) q.getSingleResult();
        return Money.of(
                tuple.get(1, BigDecimal.class).toPlainString(),
                tuple.get(0, String.class));
    }

    @Override
    public List<Account> findByIDs(Set<UUID> ids, String city, boolean forUpdate) {
        return forUpdate
                ? accountRepository.findAllWithLock(ids, city)
                : accountRepository.findAll(ids, city);
    }

    @Override
    public List<Account> findTopByCity(Collection<String> cities, int limit) {
        List<Account> accounts = new ArrayList<>();
        // No window fnc
        cities.forEach(c -> accounts.addAll(accountRepository.findAll(List.of(c),
                PageRequest.ofSize(limit)).getContent()));
        return accounts;
    }

    @Override
    public void deleteAll() {
        accountRepository.deleteAllInBatch();
    }

    @Override
    public Page<Account> findAll(Collection<String> cities, Pageable page) {
        return accountRepository.findAll(cities, page);
    }
}
