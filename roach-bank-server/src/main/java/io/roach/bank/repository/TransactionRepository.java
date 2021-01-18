package io.roach.bank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;

public interface TransactionRepository {
    Transaction create(Transaction transaction);

    Transaction findById(Transaction.Id id);

    TransactionItem getItemById(TransactionItem.Id id);

    Page<Transaction> findAll(Pageable pageable);

    Page<TransactionItem> findItems(Transaction.Id transactionId, Pageable pageable);
}
