package io.roach.bank.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;

public interface TransactionRepository {
    Transaction createTransaction(Transaction transaction);

    Transaction findTransactionById(UUID id);

    Transaction findTransactionById(UUID id, String city);

    TransactionItem findTransactionItemById(TransactionItem.Id id);

    Page<Transaction> findTransactions(Pageable pageable);

    Page<TransactionItem> findTransactionItems(UUID transactionId, Pageable pageable);

    void deleteAll();
}
