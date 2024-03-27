package io.roach.bank.repository.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.domain.Transaction;

@Transactional(propagation = Propagation.MANDATORY)
public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdAndCity(UUID id, String city);
}
