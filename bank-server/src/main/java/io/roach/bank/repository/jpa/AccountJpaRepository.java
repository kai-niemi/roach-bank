package io.roach.bank.repository.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;

@Transactional(propagation = Propagation.MANDATORY)
public interface AccountJpaRepository extends JpaRepository<Account, UUID>,
        JpaSpecificationExecutor<Account> {

    @Query(value = "select a.balance "
            + "from Account a "
            + "where a.id = ?1")
    Money findBalanceById(UUID id);

    @Query(value = "select "
            + "count (a.id), "
            + "count (distinct a.city), "
            + "sum (a.balance.amount), "
            + "min (a.balance.amount), "
            + "max (a.balance.amount), "
            + "a.balance.currency "
            + "from Account a "
            + "where a.city = ?1 "
            + "group by a.city,a.balance.currency")
    Stream<Tuple> accountSummary(String city);

    @Query(value = "select "
            + "  count (distinct t.id), "
            + "  count (t.id), "
            + "  sum (abs(ti.amount.amount)), "
            + "  sum (ti.amount.amount), "
            + "  ti.amount.currency "
            + "from Transaction t join TransactionItem ti on t.id = ti.id.transactionId "
            + "where ti.city = ?1 "
            + "group by ti.city, ti.amount.currency")
    Stream<Tuple> transactionSummary(String city);

    @Query(value = "select a "
            + "from Account a "
            + "where a.id in (?1) and a.city = ?2")
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Account> findAllWithLock(Set<UUID> ids, String city);

    @Query(value = "select a "
            + "from Account a "
            + "where a.id in (?1) and a.city = ?2")
    List<Account> findAll(Set<UUID> ids, String city);

    @Query(value
            = "select a "
            + "from Account a "
            + "where a.city in (:cities)",
            countQuery
                    = "select count(a.id) "
                    + "from Account a "
                    + "where a.city in (:cities)")
    Page<Account> findAll(@Param("cities") Collection<String> cities, Pageable pageable);
}
