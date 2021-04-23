package io.roach.bank.service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import io.roach.bank.annotation.TransactionControlService;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.AccountClosedException;
import io.roach.bank.domain.BadRequestException;
import io.roach.bank.domain.NoSuchAccountException;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.util.Pair;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.repository.TransactionRepository;

@Service
@TransactionControlService
public class DefaultBankService implements BankService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Transaction createTransaction(Transaction.Id id, TransactionForm request) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No transaction context - check Spring profile settings");
        }

        if (request.getAccountLegs().size() < 2) {
            throw new BadRequestException("Must have at least two account items");
        }

        // Coalesce multi-legged transactions
        final Map<Account.Id, Pair<Money, String>> legs = coalesce(request);

        // Lookup accounts with authoritative reads
        final List<Account> accounts = accountRepository.findAccountsForUpdate(legs.keySet());

        final Transaction.Builder transactionBuilder = Transaction.builder()
                .withId(id)
                .withTransactionType(request.getTransactionType())
                .withBookingDate(request.getBookingDate())
                .withTransferDate(request.getTransferDate());

        legs.forEach((accountId, value) -> {
            final Money amount = value.getLeft();

            Account account = accounts.stream().filter(a -> Objects.equals(a.getId(), accountId))
                    .findFirst().orElseThrow(() -> new NoSuchAccountException(accountId.toString()));
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

        return transactionRepository.create(transactionBuilder.build());
    }

    private Map<Account.Id, Pair<Money, String>> coalesce(TransactionForm request) {
        final Map<Account.Id, Pair<Money, String>> legs = new HashMap<>();
        final Map<Currency, BigDecimal> amounts = new HashMap<>();

        // Compact accounts and verify that total balance for the legs with the same currency is zero
        request.getAccountLegs().forEach(leg -> {
            legs.compute(Account.Id.of(leg.getId(), leg.getRegion()),
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
    public Page<Transaction> find(Pageable page) {
        return transactionRepository.findAll(page);
    }

    @Override
    public Transaction findById(Transaction.Id id) {
        return transactionRepository.findById(id);
    }

    @Override
    public TransactionItem getItemById(TransactionItem.Id id) {
        return transactionRepository.getItemById(id);
    }

    @Override
    public Page<TransactionItem> findItemsByTransactionId(Transaction.Id transactionId, Pageable page) {
        return transactionRepository.findItems(transactionId, page);
    }

    @Override
    public void deleteAll() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }
}
