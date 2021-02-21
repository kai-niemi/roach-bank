package io.roach.bank.repository.jpa;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionControlService;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.repository.ReportingRepository;

@Profile(ProfileNames.JPA)
@TransactionControlService
public class JpaReportingRepository implements ReportingRepository {
    @Autowired
    private AccountJpaRepository accountRepository;

    private void assertTransactionActive() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");
    }

    @Override
    public AccountSummary accountSummary(Currency currency, List<String> regions) {
        List<AccountSummary> result = new LinkedList<>();

        try (Stream<Tuple> stream = accountRepository.accountSummary(currency, regions)) {
            stream.forEach(o -> {
                AccountSummary summary = new AccountSummary();
                summary.setCurrency(currency);
                summary.setNumberOfAccounts(o.get(0, Integer.class));
                summary.setNumberOfRegions(o.get(1, Integer.class));
                summary.setTotalBalance(o.get(2, BigDecimal.class));
                summary.setMinBalance(o.get(3, BigDecimal.class));
                summary.setMaxBalance(o.get(4, BigDecimal.class));

                result.add(summary);
            });
        }

        return result.iterator().next();
    }

    @Override
    public TransactionSummary transactionSummary(Currency currency, List<String> regions) {
        assertTransactionActive();

        List<TransactionSummary> result = new LinkedList<>();

        try (Stream<Tuple> stream = accountRepository.transactionSummary(currency, regions)) {
            stream.forEach(o -> {
                TransactionSummary summary = new TransactionSummary();
                summary.setCurrency(currency);
                summary.setNumberOfTransactions(o.get(0, Integer.class));
                summary.setNumberOfLegs(o.get(1, Integer.class));
                summary.setTotalTurnover(o.get(2, BigDecimal.class));

                result.add(summary);
            });
        }

        return result.iterator().next();
    }
}
