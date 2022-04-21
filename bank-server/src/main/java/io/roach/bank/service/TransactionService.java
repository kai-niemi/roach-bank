package io.roach.bank.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;

public interface TransactionService {
    Transaction createTransaction(UUID id, TransactionForm transactionRequest);

    Transaction findById(UUID id);

    TransactionItem getItemById(TransactionItem.Id id);

    Page<Transaction> find(Pageable page);

    Page<TransactionItem> findItemsByTransactionId(UUID transactionId, Pageable page);

    void deleteAll();
}
