package io.roach.bank.repository.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.domain.Transaction;

@TransactionMandatory
public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {
}
