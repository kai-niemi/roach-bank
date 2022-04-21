package io.roach.bank.service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.repository.TransactionRepository;
import io.roach.bank.util.Pair;

@Service
public class DefaultTransactionService implements TransactionService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @TransactionMandatory
    public Transaction createTransaction(UUID id, TransactionForm form) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No transaction context - check Spring profile settings");
        }

        if (form.getAccountLegs().size() < 2) {
            throw new BadRequestException("Must have at least two account items");
        }

        // Coalesce multi-legged transactions
        final Map<UUID, Pair<Money, String>> legs = coalesce(form);

        // Lookup accounts with authoritative reads
        final List<Account> accounts = accountRepository.findAccountsById(legs.keySet(), form.isSelectForUpdate());

        final Transaction.Builder transactionBuilder = Transaction.builder()
                .withId(id)
                .withCity(form.getCity())
                .withTransactionType(form.getTransactionType())
                .withBookingDate(form.getBookingDate())
                .withTransferDate(form.getTransferDate());

        legs.forEach((accountId, value) -> {
            final Money amount = value.getLeft();

            Account account = accounts.stream().filter(a -> Objects.equals(a.getId(), accountId))
                    .findFirst().orElseThrow(() -> new NoSuchAccountException(accountId));
            if (account.isClosed()) {
                throw new AccountClosedException(account.toDisplayString());
            }

            transactionBuilder
                    .andItem()
                    .withAccount(account)
                    .withRunningBalance(account.getBalance())
                    .withAmount(amount)
                    .withNote(value.getRight())
                    .then();

            account.addAmount(amount);
        });

        accountRepository.updateBalances(accounts);

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
                            : Pair.of(amount.getLeft().plus(leg.getAmount()), leg.getNote()));
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
    @TransactionMandatory
    public Page<Transaction> find(Pageable page) {
        return transactionRepository.findTransactions(page);
    }

    @Override
    @TransactionMandatory
    public Transaction findById(UUID id) {
        return transactionRepository.findTransactionById(id);
    }

    @Override
    @TransactionMandatory
    public TransactionItem getItemById(TransactionItem.Id id) {
        return transactionRepository.getTransactionItemById(id);
    }

    @Override
    @TransactionMandatory
    public Page<TransactionItem> findItemsByTransactionId(UUID transactionId, Pageable page) {
        return transactionRepository.findTransactionItems(transactionId, page);
    }

    @Override
    @TransactionMandatory
    public void deleteAll() {
        transactionRepository.deleteAll();
    }
}
