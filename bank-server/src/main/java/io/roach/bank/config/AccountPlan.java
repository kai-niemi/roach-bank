package io.roach.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "roachbank.account-plan")
public class AccountPlan {
    private boolean clearAtStartup;

    private boolean clearTransitive;

    private String region;

    private int numAccountsPerCity;

    private String initialBalance;

    private String currency;

    public boolean isClearAtStartup() {
        return clearAtStartup;
    }

    public void setClearAtStartup(boolean clearAtStartup) {
        this.clearAtStartup = clearAtStartup;
    }

    public boolean isClearTransitive() {
        return clearTransitive;
    }

    public void setClearTransitive(boolean clearTransitive) {
        this.clearTransitive = clearTransitive;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getNumAccountsPerCity() {
        return numAccountsPerCity;
    }

    public void setNumAccountsPerCity(int numAccountsPerCity) {
        this.numAccountsPerCity = numAccountsPerCity;
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
                ", clearTransitive=" + clearTransitive +
                ", region='" + region + '\'' +
                ", numAccountsPerCity=" + numAccountsPerCity +
                ", initialBalance='" + initialBalance + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
