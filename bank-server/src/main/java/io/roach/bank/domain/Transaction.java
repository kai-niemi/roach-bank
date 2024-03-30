package io.roach.bank.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Represents a monetary transaction (balance update) between at least two different accounts.
 * <p>
 * JPA annotations are only used by JPA server implementation.
 */
@Entity
@Table(name = "transaction")
@DynamicInsert
public class Transaction extends AbstractEntity<UUID> {
    @Id
    private UUID id;

    @Column(name = "city")
    private String city;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transfer_date", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transferDate;

    @Column(name = "booking_date", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @OneToMany(orphanRemoval = true, mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<TransactionItem> items;

    public Transaction() {
    }

    protected Transaction(String city,
                          String transactionType,
                          LocalDate bookingDate, LocalDate transferDate,
                          List<TransactionItem> items) {
        this.city = city;
        this.transactionType = transactionType;
        this.bookingDate = bookingDate;
        this.transferDate = transferDate;
        this.items = items;
        this.items.forEach(item -> item.link(this));
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    protected void onCreate() {
        if (bookingDate == null) {
            bookingDate = LocalDate.now();
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getTransactionType() {
        return transactionType;
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<TransactionItem> items = new ArrayList<>();

        private String city;

        private String transactionType;

        private LocalDate bookingDate;

        private LocalDate transferDate;

        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withTransactionType(String transactionType) {
            this.transactionType = transactionType;
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

        public Builder withItems(List<TransactionItem> items) {
            this.items.addAll(items);
            return this;
        }

        public TransactionItem.Builder andItem() {
            return TransactionItem.builder(this, items::add);
        }

        public Transaction build() {
            return new Transaction(city, transactionType, bookingDate, transferDate, items);
        }
    }
}
