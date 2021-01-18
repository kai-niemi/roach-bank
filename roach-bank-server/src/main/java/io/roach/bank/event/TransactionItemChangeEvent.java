package io.roach.bank.event;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionItemChangeEvent extends AbstractChangeEvent {
    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("running_balance")
    private BigDecimal runningBalance;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionItemChangeEvent that = (TransactionItemChangeEvent) o;
        return accountId.equals(that.accountId) &&
                transactionId.equals(that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, transactionId);
    }

    public UUID getAccountId() {
        return accountId;
    }

    public TransactionItemChangeEvent setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public TransactionItemChangeEvent setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionItemChangeEvent setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionItemChangeEvent setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public TransactionItemChangeEvent setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
        return this;
    }

    @Override
    public String toString() {
        return "TransactionLegChangeEvent{" +
                "accountId=" + accountId +
                ", transactionId=" + transactionId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", runningBalance=" + runningBalance +
                "} " + super.toString();
    }
}
