package io.roach.bank.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.roach.bank.annotation.TransactionControlService;
import io.roach.bank.domain.Transaction;

@TransactionControlService
public interface TransactionJpaRepository extends JpaRepository<Transaction, Transaction.Id>,
        JpaSpecificationExecutor<Transaction> {
}
