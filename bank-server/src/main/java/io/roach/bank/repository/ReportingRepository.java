package io.roach.bank.repository;

import java.util.Currency;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;

public interface ReportingRepository {
    AccountSummary accountSummary(Currency currency);

    TransactionSummary transactionSummary(Currency currency);
}
