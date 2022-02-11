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
public interface AccountJpaRepository extends JpaRepository<Account, Account.Id>,
        JpaSpecificationExecutor<Account> {

    @Query(value = "select a.balance "
            + "from Account a "
            + "where a.id = ?1")
    Money findBalanceById(Account.Id id);

    @Query(value = "select a.currency,a.balance "
            + "from account a "
            + "as of system time follower_read_timestamp() "
            + "where a.id = ?1 and a.region = ?2", nativeQuery = true)
    @TransactionNotAllowed
    Tuple findBalanceSnapshot(String id, String region);

    @Query(value = "select "
            + "count (a.id.uuid), "
            + "count (distinct a.id.region), "
            + "sum (a.balance.amount), "
            + "min (a.balance.amount), "
            + "max (a.balance.amount) "
            + "from Account a "
            + "where a.balance.currency = ?1 and a.id.region in (?2)")
    Stream<Tuple> accountSummary(Currency currency, List<String> regions);

    @Query(value = "select "
            + "  count (distinct t.id), "
            + "  count (t.id), "
            + "  sum (ti.amount.amount) "
            + "from Transaction t join TransactionItem ti "
            + "where ti.account.balance.currency = ?1 "
            + "and ti.transaction.id.region in (?2) "
            + "and ti.transaction.id.region=t.id.region")
    Stream<Tuple> transactionSummary(Currency currency, List<String> regions);

    @Query(value = "select a "
            + "from Account a "
            + "where a.id.uuid in (?1) and a.id.region in (?2)")
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Account> findAll(Set<UUID> ids, Set<String> regions);

    @Query(value
            = "select a "
            + "from Account a "
            + "where a.id.region in (:regions)",
            countQuery
                    = "select count(a.id.uuid) "
                    + "from Account a "
                    + "where a.id.region in (:regions)")
    Page<Account> findAll(Pageable pageable, @Param("regions") List<String> regions);
}
