package io.roach.bank.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.domain.Transaction;

@Service
public class TransactionServiceFacade {
    @Autowired
    private TransactionService transactionService;

    @TransactionBoundary
    @Retryable(retryAttempts = 30)
    public Transaction createTransaction(UUID id, TransactionForm transactionForm) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Expected transaction");
        return transactionService.createTransaction(id, transactionForm);
    }
}
