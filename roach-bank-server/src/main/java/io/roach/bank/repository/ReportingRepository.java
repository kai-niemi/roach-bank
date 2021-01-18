package io.roach.bank.repository;

import java.util.Currency;
import java.util.List;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;

public interface ReportingRepository {
    AccountSummary accountSummary(Currency currency, List<String> regions);

    TransactionSummary transactionSummary(Currency currency, List<String> regions);
}
