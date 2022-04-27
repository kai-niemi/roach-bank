package io.roach.bank.repository.jpa;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.repository.ReportingRepository;

@Profile(ProfileNames.JPA)
@TransactionMandatory
public class JpaReportingRepository implements ReportingRepository {
    @Autowired
    private AccountJpaRepository accountRepository;

    private void assertTransactionActive() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");
    }

    @Override
    public AccountSummary accountSummary(String city) {
        List<AccountSummary> result = new LinkedList<>();

        try (Stream<Tuple> stream = accountRepository.accountSummary(city)) {
            stream.forEach(o -> {
                AccountSummary summary = new AccountSummary();
                summary.setCity(city);
                summary.setNumberOfAccounts(o.get(0, Integer.class));
                summary.setTotalBalance(o.get(1, BigDecimal.class));
                summary.setMinBalance(o.get(2, BigDecimal.class));
                summary.setMaxBalance(o.get(3, BigDecimal.class));
                summary.setCurrency(Currency.getInstance(o.get(4, String.class)));

                result.add(summary);
            });
        }

        return result.iterator().next();
    }

    @Override
    public TransactionSummary transactionSummary(String city) {
        assertTransactionActive();

        List<TransactionSummary> result = new LinkedList<>();

        try (Stream<Tuple> stream = accountRepository.transactionSummary(city)) {
            stream.forEach(o -> {
                TransactionSummary summary = new TransactionSummary();
                summary.setCity(city);
                summary.setNumberOfTransactions(o.get(0, Integer.class));
                summary.setNumberOfLegs(o.get(1, Integer.class));
                summary.setTotalTurnover(o.get(2, BigDecimal.class));

                result.add(summary);
            });
        }

        return result.iterator().next();
    }
}
