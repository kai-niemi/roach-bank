package io.roach.bank;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public class AccountPlan {
    private int accountsPerCity;

    @NotNull
    private String initialBalance;

    @NotNull
    private String currency;

    public int getAccountsPerCity() {
        return accountsPerCity;
    }

    public void setAccountsPerCity(int accountsPerCity) {
        this.accountsPerCity = accountsPerCity;
    }

    public String getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(String initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "AccountPlan{" +
                "accountsPerCity=" + accountsPerCity +
                ", initialBalance='" + initialBalance + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
