package io.roach.bank.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.*;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Represents a monetary transaction (balance update) between at least two different accounts.
 * <p>
 * JPA annotations are only used by JPA server implementation.
 */
@Entity
@Table(name = "transaction")
public class Transaction extends AbstractEntity<Transaction.Id> {
    @EmbeddedId
    private Transaction.Id id = new Transaction.Id();

    @Column(name = "transaction_type")
    private String transferType;

    @Column(name = "transfer_date", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate transferDate;

    @Column(name = "booking_date", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @OneToMany(orphanRemoval = true, mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<TransactionItem> items;

    public Transaction() {
    }

    public Transaction(Id id) {
        this.id = id;
        this.items = Collections.emptyList();
    }

    protected Transaction(Id id,
                          String transferType,
                          LocalDate bookingDate,
                          LocalDate transferDate,
                          List<TransactionItem> items) {
        this.id = id;
        this.transferType = transferType;
        this.bookingDate = bookingDate;
        this.transferDate = transferDate;
        this.items = items;

        items.forEach(item -> {
            item.setId(new TransactionItem.Id(
                    Objects.requireNonNull(item.getAccount().getId()),
                    Objects.requireNonNull(getId())
            ));
            item.setTransaction(this);
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    protected void onCreate() {
        if (bookingDate == null) {
            bookingDate = LocalDate.now();
        }
    }

    @Override
    public Id getId() {
        return id;
    }

    public UUID getUUID() {
        return id.getUUID();
    }

    public String getRegion() {
        return id.getRegion();
    }

    public String getTransferType() {
        return transferType;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public List<TransactionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transferType='" + transferType + '\'' +
                ", transferDate=" + transferDate +
                ", bookingDate=" + bookingDate +
                ", items=<..>" +
                '}';
    }

    public static final class Builder {
        private final List<TransactionItem> items = new ArrayList<>();

        private Transaction.Id transactionId;

        private String transferType;

        private LocalDate bookingDate;

        private LocalDate transferDate;

        public Builder withId(Transaction.Id id) {
            this.transactionId = id;
            return this;
        }

        public Builder withTransferType(String transferType) {
            this.transferType = transferType;
            return this;
        }

        public Builder withBookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }

        public Builder withTransferDate(LocalDate transferDate) {
            this.transferDate = transferDate;
            return this;
        }

        public TransactionItem.Builder andItem() {
            return TransactionItem.builder(this, items::add);
        }

        public Transaction build() {
            return new Transaction(transactionId, transferType, bookingDate, transferDate, items);
        }
    }

    @Embeddable
    public static class Id implements Serializable {
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

        public static Transaction.Id of(String region) {
            return of(UUID.randomUUID(), region);
        }

        public static Transaction.Id of(UUID transactionId, String region) {
            return new Transaction.Id(transactionId, region);
        }

        public String getRegion() {
            return region;
        }

        public UUID getUUID() {
            return uuid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Id id = (Id) o;
            return uuid.equals(id.uuid) &&
                    region.equals(id.region);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, region);
        }

        @Override
        public String toString() {
            return uuid + "::" + region;
        }
    }
}
