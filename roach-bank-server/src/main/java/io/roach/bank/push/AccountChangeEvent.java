package io.roach.bank.push;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountChangeEvent extends AbstractChangeEvent {
    @JsonProperty("after")
    private Fields after = new Fields();

    private String href;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public UUID getId() {
        return after.id;
    }

    public String getName() {
        return after.name;
    }

    public String getRegion() {
        return after.region;
    }

    public BigDecimal getBalance() {
        return after.balance;
    }

    public Currency getCurrency() {
        return after.currency != null ? Currency.getInstance(after.currency) : null;
    }

    public Fields getAfter() {
        return after;
    }

    public void setAfter(Fields after) {
        this.after = after;
    }

    @Override
    public String toString() {
        return "AccountChangeEvent{" +
                "id=" + after.id +
                ", name='" + after.name + '\'' +
                ", currency='" + after.currency + '\'' +
                ", balance=" + after.balance +
                "} " + super.toString();
    }

    public static class Fields {
        // Account ID
        private UUID id;

        // Account name
        private String name;

        // Currency code
        private String currency;

        private String region;

        // Latest account balance
        private BigDecimal balance;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getRegion() {
            return region;
        }

        public Fields setRegion(String region) {
            this.region = region;
            return this;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }
}
