package io.roach.bank.changefeed.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountPayload extends AbstractPayload {
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

    public String getCity() {
        return after.city;
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
        return "AccountPayload{" +
                "id=" + after.id +
                ", city='" + after.city + '\'' +
                ", name='" + after.name + '\'' +
                ", currency='" + after.currency + '\'' +
                ", balance=" + after.balance +
                "} " + super.toString();
    }

    public static class Fields {
        private UUID id;

        private String name;

        private String city;

        private BigDecimal balance;

        private String currency;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getCity() {
            return city;
        }

        public Fields setCity(String city) {
            this.city = city;
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
