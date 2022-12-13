package io.roach.bank.repository.jpa;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;

@Service
@Transactional(propagation = Propagation.MANDATORY)
@Profile(ProfileNames.JPA)
public class JpaAccountRepository implements AccountRepository {
    @Autowired
    private AccountJpaRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    public void updateBalances(List<Pair<UUID, BigDecimal>> balanceUpdates) {
        int[] rowsAffected = jdbcTemplate.batchUpdate(
                "UPDATE account "
                        + "SET "
                        + "   balance = balance + ?,"
                        + "   updated = clock_timestamp() "
                        + "WHERE id = ? "
                        + "   AND closed=false "
                        + "   AND (balance + ?) * abs(allow_negative-1) >= 0",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Pair<UUID, BigDecimal> entry = balanceUpdates.get(i);
                        ps.setBigDecimal(1, entry.getSecond());
                        ps.setObject(2, entry.getFirst());
                        ps.setBigDecimal(3, entry.getSecond());
                    }

                    @Override
                    public int getBatchSize() {
                        return balanceUpdates.size();
                    }
                });

        // Check invariant on neg balance
        Arrays.stream(rowsAffected)
                .filter(i -> i != 1)
                .forEach(i -> {
                    throw new IncorrectResultSizeDataAccessException(1, i);
                });

//        balanceUpdates.forEach(pair -> {
//            Query q = entityManager.createNativeQuery("UPDATE account"
//                    + " SET "
//                    + "   balance = balance + ?,"
//                    + "   updated = clock_timestamp()"
//                    + " WHERE id = ?"
//                    + "   AND closed=false"
//                    + "   AND (balance + ?) * abs(allow_negative-1) >= 0");
//            q.setParameter(1, pair.getSecond());
//            q.setParameter(2, pair.getFirst());
//            q.setParameter(3, pair.getSecond());
//            int r = q.executeUpdate();
//            if (r != 1) {
//                throw new IncorrectResultSizeDataAccessException(1, r);
//            }
//        });
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
    public Account getAccountByReference(UUID id) {
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
        Tuple tuple = accountRepository.findBalanceSnapshot(id.toString());
        return Money.of(
                tuple.get(1, BigDecimal.class).toPlainString(),
                tuple.get(0, String.class));
    }

    @Override
    public List<Account> findByIDs(Set<UUID> ids, boolean locking) {
        return locking ? accountRepository.findAllWithLock(ids)
                : accountRepository.findAll(ids);
    }

    @Override
    public Page<Account> findPageByCity(Set<String> cities, Pageable page) {
        return accountRepository.findAll(page, new ArrayList<>(cities));
    }

    @Override
    public List<Account> findByCity(String city, int limit) {
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
