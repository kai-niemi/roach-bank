package io.roach.bank.repository.jpa;

import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.LockModeType;
import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.annotation.TransactionNotAllowed;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;

@TransactionMandatory
public interface AccountJpaRepository extends JpaRepository<Account, UUID>,
        JpaSpecificationExecutor<Account> {

    @Query(value = "select a.balance "
            + "from Account a "
            + "where a.id = ?1")
    Money findBalanceById(UUID id);

    @Query(value = "select a.currency,a.balance "
            + "from account a "
            + "as of system time follower_read_timestamp() "
            + "where a.id = ?1", nativeQuery = true)
    @TransactionNotAllowed
    Tuple findBalanceSnapshot(String id);

    @Query(value = "select "
            + "count (a.id), "
            + "count (distinct a.city), "
            + "sum (a.balance.amount), "
            + "min (a.balance.amount), "
            + "max (a.balance.amount) "
            + "from Account a "
            + "where a.balance.currency = ?1")
    Stream<Tuple> accountSummary(Currency currency);

    @Query(value = "select "
            + "  count (distinct t.id), "
            + "  count (t.id), "
            + "  sum (ti.amount.amount) "
            + "from Transaction t join TransactionItem ti "
            + "where ti.account.balance.currency = ?1")
    Stream<Tuple> transactionSummary(Currency currency);

    @Query(value = "select a "
            + "from Account a "
            + "where a.id in (?1) and a.city=?2")
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Account> findAllWithLock(Set<UUID> ids, String city);

    @Query(value = "select a "
            + "from Account a "
            + "where a.id in (?1) and a.city=?2")
    List<Account> findAll(Set<UUID> ids, String city);

    @Query(value
            = "select a "
            + "from Account a "
            + "where a.city in (:cities)",
            countQuery
                    = "select count(a.id) "
                    + "from Account a "
                    + "where a.city in (:cities)")
    Page<Account> findAll(Pageable pageable, @Param("cities") List<String> cities);
}
