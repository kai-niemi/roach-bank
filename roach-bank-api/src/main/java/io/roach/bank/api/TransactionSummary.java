package io.roach.bank.api;

import java.math.BigDecimal;
import java.util.Currency;

public class TransactionSummary {
    private Currency currency;

    private int numberOfTransactions;

    private int numberOfLegs;

    private BigDecimal totalTurnover;

    private BigDecimal totalCheckSum;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public int getNumberOfLegs() {
        return numberOfLegs;
    }

    public void setNumberOfLegs(int numberOfLegs) {
        this.numberOfLegs = numberOfLegs;
    }

    public BigDecimal getTotalTurnover() {
        return totalTurnover;
    }

    public void setTotalTurnover(BigDecimal totalTurnover) {
        this.totalTurnover = totalTurnover;
    }

    public BigDecimal getTotalCheckSum() {
        return totalCheckSum;
    }

    public void setTotalCheckSum(BigDecimal totalCheckSum) {
        this.totalCheckSum = totalCheckSum;
    }

    @Override
    public String toString() {
        return "TransactionSummary{" +
                "currency=" + currency +
                ", numberOfTransactions=" + numberOfTransactions +
                ", numberOfLegs=" + numberOfLegs +
                ", totalTurnover=" + totalTurnover +
                ", totalCheckSum=" + totalCheckSum +
                '}';
    }
}
