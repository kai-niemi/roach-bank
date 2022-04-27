package io.roach.bank.repository;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;

public interface ReportingRepository {
    AccountSummary accountSummary(String city);

    TransactionSummary transactionSummary(String city);
}
