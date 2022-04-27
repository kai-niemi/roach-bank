package io.roach.bank.api;

import java.math.BigDecimal;
import java.util.Currency;

public class AccountSummary {
    private String city;

    private Currency currency;

    private int numberOfAccounts;

    private BigDecimal totalBalance;

    private BigDecimal minBalance;

    private BigDecimal maxBalance;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public int getNumberOfAccounts() {
        return numberOfAccounts;
    }

    public void setNumberOfAccounts(int numberOfAccounts) {
        this.numberOfAccounts = numberOfAccounts;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getMinBalance() {
        return minBalance;
    }

    public void setMinBalance(BigDecimal minBalance) {
        this.minBalance = minBalance;
    }

    public BigDecimal getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(BigDecimal maxBalance) {
        this.maxBalance = maxBalance;
    }

    @Override
    public String toString() {
        return "AccountSummary{" +
                "region=" + city +
                ", numberOfAccounts=" + numberOfAccounts +
                ", totalBalance=" + totalBalance +
                ", minBalance=" + minBalance +
                ", maxBalance=" + maxBalance +
                '}';
    }
}
