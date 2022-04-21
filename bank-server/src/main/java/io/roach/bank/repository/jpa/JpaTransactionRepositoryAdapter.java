package io.roach.bank.repository.jpa;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.repository.TransactionRepository;

@Repository
@TransactionMandatory
@Profile(ProfileNames.JPA)
public class JpaTransactionRepositoryAdapter implements TransactionRepository {
    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private TransactionItemJpaRepository itemRepository;

    @Override
    public Transaction createTransaction(Transaction transaction) {
        transaction.getItems().forEach(transactionItem -> itemRepository.save(transactionItem));
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction findTransactionById(UUID id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Transaction> findTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public TransactionItem getTransactionItemById(TransactionItem.Id id) {
        return itemRepository.getById(id);
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
