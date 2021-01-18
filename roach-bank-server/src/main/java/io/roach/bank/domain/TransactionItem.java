package io.roach.bank.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import javax.persistence.*;

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
public class TransactionItem extends AbstractEntity<TransactionItem.Id> {
    @EmbeddedId
    private Id id = new Id();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "amount", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "currency", length = 3, nullable = false,
                            updatable = false))
    })
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "running_balance", nullable = false, updatable = false)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "currency", length = 3, nullable = false, insertable = false,
                            updatable = false))
    })
    private Money runningBalance;

    @Column(name = "note", length = 128, updatable = false)
    @Basic(fetch = FetchType.LAZY)
    private String note;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "account_region", referencedColumnName = "region", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Account account;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "transaction_region", referencedColumnName = "region", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Transaction transaction;

    protected TransactionItem() {
    }

    public static Builder builder(Transaction.Builder parentBuilder, Consumer<TransactionItem> callback) {
        return new Builder(parentBuilder, callback);
    }

    @Override
    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Money getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(Money runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "id=" + id +
                ", amount=" + amount +
                ", runningBalance=" + runningBalance +
                ", note='" + note + '\'' +
                '}';
    }

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "account_id", updatable = false)
        private UUID accountId;

        @Column(name = "account_region", updatable = false)
        private String accountRegion;

        @Column(name = "transaction_id", updatable = false)
        private UUID transactionId;

        @Column(name = "transaction_region", updatable = false)
        private String transactionRegion;

        protected Id() {
        }

        protected Id(Account.Id accountId, Transaction.Id transactionId) {
            this.accountId = accountId.getUUID();
            this.accountRegion = accountId.getRegion();
            this.transactionId = transactionId.getUUID();
            this.transactionRegion = transactionId.getRegion();
        }

        public static Id of(Account.Id accountId, Transaction.Id transactionId) {
            return new Id(accountId, transactionId);
        }

        public UUID getAccountId() {
            return accountId;
        }

        public String getAccountRegion() {
            return accountRegion;
        }

        public UUID getTransactionId() {
            return transactionId;
        }

        public String getTransactionRegion() {
            return transactionRegion;
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
                    Objects.equals(transactionId, id.transactionId) &&
                    Objects.equals(transactionRegion, id.transactionRegion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, transactionId, transactionRegion);
        }

        @Override
        public String toString() {
            return "Id{" +
                    "accountId=" + accountId +
                    ", accountRegion='" + accountRegion + '\'' +
                    ", transactionId=" + transactionId +
                    ", transactionRegion='" + transactionRegion + '\'' +
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
