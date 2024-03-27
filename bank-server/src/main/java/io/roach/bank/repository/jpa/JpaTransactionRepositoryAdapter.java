package io.roach.bank.repository.jpa;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.ProfileNames;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.repository.TransactionRepository;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
@Profile(ProfileNames.JPA)
public class JpaTransactionRepositoryAdapter implements TransactionRepository {
    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private TransactionItemJpaRepository itemRepository;

    @Override
    public Transaction createTransaction(Transaction transaction) {
        Transaction t = transactionRepository.save(transaction);
        transaction.getItems().forEach(transactionItem -> transactionItem.getId().setTransactionId(t.getId()));
        itemRepository.saveAll(transaction.getItems());
        return t;
    }

    @Override
    public Transaction findTransactionById(UUID id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public Transaction findTransactionById(UUID id, String city) {
        return transactionRepository.findByIdAndCity(id, city).orElse(null);
    }

    @Override
    public Page<Transaction> findTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public TransactionItem findTransactionItemById(TransactionItem.Id id) {
        return itemRepository.getReferenceById(id);
    }

    @Override
    public Page<TransactionItem> findTransactionItems(UUID id, Pageable pageable) {
        return itemRepository.findById(id, pageable);
    }

    @Override
    public void deleteAll() {
        itemRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
    }
}
