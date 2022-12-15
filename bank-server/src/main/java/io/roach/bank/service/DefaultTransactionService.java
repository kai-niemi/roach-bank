package io.roach.bank.service;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @Value("${roachbank.loadAccountByReference}")
    private boolean loadByReference;

    @Value("${roachbank.loadAccountWithSFU}")
    private boolean loadAccountWithSFU;

    @Override
    public Transaction createTransaction(UUID id, TransactionForm transactionForm) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No transaction context - check Spring profile settings");
        }

        // Short-circuit
        if (transactionForm.isFake()) {
            return Transaction.builder().withId(id).build();
        }

        if (transactionForm.getAccountLegs().size() < 2) {
            throw new BadRequestException("Must have at least two account legs");
        }

        // Coalesce multi-legged transactions
        final Transaction.Builder transactionBuilder = Transaction.builder()
                .withId(id)
                .withCity(transactionForm.getCity())
                .withTransactionType(transactionForm.getTransactionType())
                .withBookingDate(transactionForm.getBookingDate())
                .withTransferDate(transactionForm.getTransferDate());

        final List<Pair<UUID, BigDecimal>> balanceUpdates = new ArrayList<>();

        final Map<UUID, Pair<Money, String>> legs = coalesce(transactionForm);

        List<Account> accounts;

        if (!loadByReference) {
            Set<UUID> accountIds = new HashSet<>();
            legs.forEach((accountId, value) -> accountIds.add(accountId));
            accounts = accountRepository.findByIDs(accountIds, loadAccountWithSFU);
        } else {
            accounts = Collections.emptyList();
        }

        legs.forEach((accountId, value) -> {
            final Money amount = value.getFirst();

            Account account;
            Money runningBalance;

            // Get by reference to avoid SELECT but no running balance
            if (loadByReference) {
                account = accountRepository.getAccountByReference(accountId);
                runningBalance = Money.zero(Money.USD);
            } else {
                account = accounts.stream()
                        .filter(a -> a.getId().equals(accountId))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchAccountException(accountId));
                runningBalance = account.getBalance();
            }

            transactionBuilder
                    .andItem()
                    .withAccount(account)
                    .withAmount(amount)
                    .withNote(value.getSecond())
                    .withRunningBalance(runningBalance)
                    .then();

            balanceUpdates.add(Pair.of(accountId, amount.getAmount()));
        });

        try {
            accountRepository.updateBalances(balanceUpdates);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NegativeBalanceException("Negative balance check failed", e);
        }

        return transactionRepository.createTransaction(transactionBuilder.build());
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
    public TransactionItem getItemById(TransactionItem.Id id) {
        return transactionRepository.getTransactionItemById(id);
    }

    @Override
    public Page<TransactionItem> findItemsByTransactionId(UUID transactionId, Pageable page) {
        return transactionRepository.findTransactionItems(transactionId, page);
    }

    @Override
    public void deleteAll() {
        transactionRepository.deleteAll();
    }
}
