package io.roach.bank.api;

import java.math.BigDecimal;

public class TransactionSummary {
    private String city;

    private int numberOfTransactions;

    private int numberOfLegs;

    private BigDecimal totalTurnover;

    private BigDecimal totalCheckSum;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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
                "city=" + city +
                ", numberOfTransactions=" + numberOfTransactions +
                ", numberOfLegs=" + numberOfLegs +
                ", totalTurnover=" + totalTurnover +
                ", totalCheckSum=" + totalCheckSum +
                '}';
    }
}
