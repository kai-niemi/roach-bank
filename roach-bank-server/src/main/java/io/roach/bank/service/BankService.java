package io.roach.bank.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;

public interface BankService {
    Transaction createTransaction(Transaction.Id id, TransactionForm transactionRequest);

    Transaction findById(Transaction.Id id);

    TransactionItem getItemById(TransactionItem.Id id);

    Page<Transaction> find(Pageable page);

    Page<TransactionItem> findItemsByTransactionId(Transaction.Id transactionId, Pageable page);

    void deleteAll();
}
