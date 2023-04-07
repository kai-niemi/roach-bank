package io.roach.bank.repository.jpa;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.repository.ReportingRepository;
import jakarta.persistence.Tuple;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
@Profile(ProfileNames.JPA)
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
                summary.setNumberOfAccounts(o.get(0, Long.class));
                summary.setTotalBalance(o.get(2, BigDecimal.class));
                summary.setMinBalance(o.get(3, BigDecimal.class));
                summary.setMaxBalance(o.get(4, BigDecimal.class));
                summary.setCurrency(o.get(5, Currency.class));

                result.add(summary);
            });
        }
        if (result.isEmpty()) {
            AccountSummary summary = new AccountSummary();
            summary.setCity(city);
            return summary;
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
                summary.setCurrency(Currency.getInstance(o.get(4, String.class)));
                summary.setNumberOfTransactions(o.get(0, Long.class));
                summary.setNumberOfLegs(o.get(1, Long.class));

                BigDecimal sum = o.get(2, BigDecimal.class);
                summary.setTotalTurnover(sum != null ? sum : BigDecimal.ZERO);

                BigDecimal checksum = o.get(3, BigDecimal.class);
                summary.setTotalCheckSum(checksum != null ? checksum : BigDecimal.ZERO);

                result.add(summary);
            });
        }
        if (result.isEmpty()) {
            TransactionSummary summary = new TransactionSummary();
            summary.setCity(city);
            return summary;
        }
        return result.iterator().next();
    }
}
