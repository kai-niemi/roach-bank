package io.roach.bank.repository.jpa;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.domain.TransactionItem;

@TransactionMandatory
public interface TransactionItemJpaRepository extends JpaRepository<TransactionItem, TransactionItem.Id>,
        JpaSpecificationExecutor<TransactionItem> {

    @Query(value
            = "select item from TransactionItem item "
            + "where item.transaction.id.uuid = :transactionId",
            countQuery
                    = "select count(item.id.transactionId) from TransactionItem item "
                    + "where item.transaction.id.uuid = :transactionId")
    Page<TransactionItem> findById(
            @Param("transactionId") UUID transactionId,
            Pageable page);
}
