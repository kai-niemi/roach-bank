package io.roach.bank.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicInsert;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.roach.bank.api.support.Money;

/**
 * Immutable transaction item/leg representing a single account balance update as part
 * of a balanced, multi-legged monetary transaction. Mapped as join with attributes
 * between account and transaction entities.
 * <p>
 * JPA annotations are only used by JPA server implementation.
 */
@Entity
@Table(name = "transaction_item")
@DynamicInsert
public class TransactionItem extends AbstractEntity<TransactionItem.Id> {
    @EmbeddedId
    private Id id = new Id();

    @Column(name = "city")
    private String city;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "amount", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "currency", length = 3, nullable = false,
                            updatable = false))
    })
    private Money amount;

    @Column(name = "note", length = 128, updatable = false)
    @Basic(fetch = FetchType.LAZY)
    private String note;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Account account;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Transaction transaction;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "running_balance", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "currency", length = 3, nullable = false, insertable = false,
                            updatable = false))
    })
    private Money runningBalance;

    protected TransactionItem() {
    }

    public static Builder builder(Transaction.Builder parentBuilder, Consumer<TransactionItem> callback) {
        return new Builder(parentBuilder, callback);
    }

    @Override
    public Id getId() {
        return id;
    }

    public TransactionItem link(Transaction transaction) {
        this.transaction = transaction;
        this.city = transaction.getCity();
        this.id = new TransactionItem.Id(
                Objects.requireNonNull(account.getId()),
                Objects.requireNonNull(transaction.getId()));
        return this;
    }

    public Account getAccount() {
        return account;
    }

    public String getCity() {
        return city;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Money getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(Money runningBalance) {
        this.runningBalance = runningBalance;
    }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "id=" + id +
                ", amount=" + amount +
                ", note='" + note + '\'' +
                '}';
    }

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "account_id", updatable = false)
        private UUID accountId;

        @Column(name = "transaction_id", updatable = false)
        private UUID transactionId;

        protected Id() {
        }

        protected Id(UUID accountId, UUID transactionId) {
            this.accountId = accountId;
            this.transactionId = transactionId;
        }

        public static Id of(UUID accountId, UUID transactionId) {
            return new Id(accountId, transactionId);
        }

        public UUID getAccountId() {
            return accountId;
        }

        public UUID getTransactionId() {
            return transactionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Id)) {
                return false;
            }
            Id id = (Id) o;
            return Objects.equals(accountId, id.accountId) &&
                    Objects.equals(transactionId, id.transactionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, transactionId);
        }

        @Override
        public String toString() {
            return "Id{" +
                    "accountId=" + accountId +
                    ", transactionId=" + transactionId +
                    '}';
        }
    }

    public static class Builder {
        private final Transaction.Builder parentBuilder;

        private final Consumer<TransactionItem> callback;

        private Money amount;

        private Money runningBalance;

        private Account account;

        private String note;

        private Builder(Transaction.Builder parentBuilder, Consumer<TransactionItem> callback) {
            this.parentBuilder = parentBuilder;
            this.callback = callback;
        }

        public Builder withAmount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder withRunningBalance(Money runningBalance) {
            this.runningBalance = runningBalance;
            return this;
        }

        public Builder withAccount(Account account) {
            this.account = account;
            return this;
        }

        public Builder withNote(String note) {
            this.note = note;
            return this;
        }

        public Transaction.Builder then() {
            Assert.notNull(account, "account is null");

            TransactionItem transactionItem = new TransactionItem();
            transactionItem.setAccount(account);
            transactionItem.setAmount(amount);
            transactionItem.setRunningBalance(runningBalance);
            transactionItem.setNote(note);

            callback.accept(transactionItem);

            return parentBuilder;
        }
    }
}
