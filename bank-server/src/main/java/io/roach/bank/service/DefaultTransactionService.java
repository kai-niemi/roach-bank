package io.roach.bank.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.roach.bank.ApplicationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.repository.TransactionRepository;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class DefaultTransactionService implements TransactionService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ApplicationModel applicationModel;

    @Override
    public Transaction createTransaction(UUID id, TransactionForm transactionForm) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Expected transaction");

        if (transactionForm.getAccountLegs().size() < 2) {
            throw new BadRequestException("Must have at least two account legs");
        }

//        Transaction t = transactionRepository.findTransactionById(id, transactionForm.getCity());
//        if (t != null) {
//            return t;
//        }

        // Coalesce multi-legged transactions
        final Transaction.Builder transactionBuilder = Transaction.builder()
                .withCity(transactionForm.getCity())
                .withTransactionType(transactionForm.getTransactionType())
                .withBookingDate(transactionForm.getBookingDate())
                .withTransferDate(transactionForm.getTransferDate());

        final List<Pair<UUID, BigDecimal>> balanceUpdates = new ArrayList<>();

        final Set<UUID> accountIds = new HashSet<>();

        final Map<UUID, Pair<Money, String>> legs = coalesce(transactionForm);

        legs.forEach((accountId, value) -> accountIds.add(accountId));

        List<Account> accounts = accountRepository.findByIDs(accountIds,
                transactionForm.getCity(), applicationModel.isSelectForUpdate());

        legs.forEach((accountId, pair) -> {
            Account account = accounts.stream()
                    .filter(a -> a.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchAccountException(accountId));

            transactionBuilder
                    .andItem()
                    .withAccount(account)
                    .withRunningBalance(account.getBalance())
                    .withAmount(pair.getFirst())
                    .withNote(pair.getSecond())
                    .then();

            balanceUpdates.add(Pair.of(accountId, pair.getFirst().getAmount()));
        });

        Transaction t = transactionRepository.createTransaction(transactionBuilder.build());

        try {
            accountRepository.updateBalances(balanceUpdates, transactionForm.getCity());
        } catch (NonTransientDataAccessException e) {
            throw new NegativeBalanceException("Negative balance check failed", e);
        }

        return t;
    }

    private Map<UUID, Pair<Money, String>> coalesce(TransactionForm request) {
        final Map<UUID, Pair<Money, String>> legs = new HashMap<>();
        final Map<Currency, BigDecimal> amounts = new HashMap<>();

        // Compact accounts and verify that total balance for the legs with the same currency is zero
        request.getAccountLegs().forEach(leg -> {
            legs.compute(leg.getId(),
                    (key, amount) -> (amount == null)
                            ? Pair.of(leg.getAmount(), leg.getNote())
                            : Pair.of(amount.getFirst().plus(leg.getAmount()), leg.getNote()));
            amounts.compute(leg.getAmount().getCurrency(),
                    (currency, amount) -> (amount == null)
                            ? leg.getAmount().getAmount() : leg.getAmount().getAmount().add(amount));
        });

        // The sum of debits for all accounts must equal the corresponding sum of credits (per currency)
        amounts.forEach((key, value) -> {
            if (value.compareTo(BigDecimal.ZERO) != 0) {
                throw new BadRequestException("Unbalanced transaction: currency ["
                        + key + "], amount sum [" + value + "]");
            }
        });

        return legs;
    }

    @Override
    public Page<Transaction> find(Pageable page) {
        return transactionRepository.findTransactions(page);
    }

    @Override
    public Transaction findById(UUID id) {
        return transactionRepository.findTransactionById(id);
    }

    @Override
    public TransactionItem findItemById(UUID transactionId, UUID accountId) {
        return transactionRepository.findTransactionItemById(
                TransactionItem.Id.of(accountId, transactionId));
    }

    @Override
    public Page<TransactionItem> findItemsByTransactionId(UUID transactionId, Pageable page) {
        return transactionRepository.findTransactionItems(transactionId, page);
    }

    @Override
    public void deleteAll() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Expected transaction");
        transactionRepository.deleteAll();
    }
}
