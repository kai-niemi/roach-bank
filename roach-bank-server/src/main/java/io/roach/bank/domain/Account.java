package io.roach.bank.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.*;

import org.springframework.util.Assert;

import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;

/**
 * Represents a monetary account like asset, liability, expense, capital accounts and so forth.
 * <p>
 * JPA annotations are only used by JPA server implementation.
 */
@Entity
@Table(name = "account")
public class Account extends AbstractEntity<Account.Id> {
    @EmbeddedId
    private Account.Id id = new Account.Id();

    @Column
    private String name;

    @Column
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @Convert(converter = AccountTypeConverter.class)
    @Column(name = "type", updatable = false, nullable = false)
    private AccountType accountType;

    @Column(name = "updated")
    @Basic(fetch = FetchType.LAZY)
    private LocalDateTime updated;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "balance")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money balance;

    @Column(nullable = false)
    private boolean closed;

    @Column(nullable = false, name = "allow_negative")
    private int allowNegative;

    protected Account() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    protected void onCreate() {
        if (updated == null) {
            updated = LocalDateTime.now();
        }
    }

    @Override
    public Account.Id getId() {
        return id;
    }

    public UUID getUUID() {
        return id.getUUID();
    }

    public String getRegion() {
        return id.getRegion();
    }

    public void addAmount(Money amount) {
        Money newBalance = getBalance().plus(amount);
        if (getAllowNegative() == 0 && newBalance.isNegative()) {
            throw new NegativeBalanceException(toDisplayString());
        }
        this.balance = newBalance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getBalance() {
        return balance;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getAllowNegative() {
        return allowNegative;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", accountType=" + accountType +
                ", balance=" + balance +
                '}';
    }

    public String toDisplayString() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }

        Account that = (Account) o;

        if (!id.equals(that.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final class Builder {
        private final Account instance = new Account();

        public Builder withGeneratedId(String region) {
            withId(UUID.randomUUID(), region);
            return this;
        }

        public Builder withId(UUID accountId, String region) {
            this.instance.id = Account.Id.of(accountId, region);
            return this;
        }

        public Builder withName(String name) {
            this.instance.name = name;
            return this;
        }

        public Builder withBalance(Money balance) {
            this.instance.balance = balance;
            return this;
        }

        public Builder withAccountType(AccountType accountType) {
            this.instance.accountType = accountType;
            return this;
        }

        public Builder withClosed(boolean closed) {
            this.instance.closed = closed;
            return this;
        }

        public Builder withAllowNegative(boolean allowNegative) {
            this.instance.allowNegative = allowNegative ? 1 : 0;
            return this;
        }

        public Builder withDescription(String description) {
            this.instance.description = description;
            return this;
        }

        public Builder withUpdated(LocalDateTime updated) {
            this.instance.updated = updated;
            return this;
        }

        public Account build() {
            Assert.notNull(instance.id, "id is null");
            return instance;
        }
    }

    @Embeddable
    public static class Id implements Serializable, Comparable<Id> {
        @Column(name = "id", updatable = false)
        private UUID uuid;

        @Column(name = "region", updatable = false)
        private String region;

        protected Id() {
        }

        public Id(UUID uuid, String region) {
            Assert.notNull(uuid, "uuid is required");
            Assert.notNull(region, "region is required");
            this.uuid = uuid;
            this.region = region;
        }

        public static Id of(UUID accountId, String region) {
            return new Id(accountId, region);
        }

        public String getRegion() {
            return region;
        }

        public UUID getUUID() {
            return uuid;
        }

        @Override
        public int compareTo(Id o) {
            return o.uuid.compareTo(uuid);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Id)) {
                return false;
            }
            Id id1 = (Id) o;
            return Objects.equals(region, id1.region) &&
                    Objects.equals(uuid, id1.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(region, uuid);
        }

        @Override
        public String toString() {
            return "Id{" +
                    "uuid=" + uuid +
                    ", region=" + region +
                    '}';
        }
    }
}
