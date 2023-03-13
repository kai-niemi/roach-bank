package io.roach.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "roachbank.account-plan")
public class AccountPlan {
    private boolean clearAtStartup;

    private String region;

    private int accountsPerCity;

    private String initialBalance;

    private String currency;

    public boolean isClearAtStartup() {
        return clearAtStartup;
    }

    public void setClearAtStartup(boolean clearAtStartup) {
        this.clearAtStartup = clearAtStartup;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

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
                "clearAtStartup=" + clearAtStartup +
                ", region='" + region + '\'' +
                ", accountsPerCity=" + accountsPerCity +
                ", initialBalance='" + initialBalance + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
