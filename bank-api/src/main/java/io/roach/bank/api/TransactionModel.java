package io.roach.bank.api;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.roach.bank.api.support.LocalDateDeserializer;
import io.roach.bank.api.support.LocalDateSerializer;

import static io.roach.bank.api.BankLinkRelations.CURIE_PREFIX;

/**
 * Describes a transaction leg/item resource representation. A transaction leg
 * represents a monetary, balanced, multi-legged transaction between two
 * or more accounts.
 */
@Relation(value = CURIE_PREFIX + BankLinkRelations.TRANSACTION_REL,
        collectionRelation = CURIE_PREFIX + BankLinkRelations.TRANSACTION_LIST_REL)
@JsonPropertyOrder({"links", "transactionItems"})
public class TransactionModel extends RepresentationModel<TransactionModel> {
    private UUID transactionId;

    private String region;

    private String transactionType;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate transactionDate;

    private CollectionModel<TransactionItemModel> transactionItems;

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getRegion() {
        return region;
    }

    public TransactionModel setRegion(String region) {
        this.region = region;
        return this;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public CollectionModel<TransactionItemModel> getTransactionItems() {
        return transactionItems;
    }

    public void setTransactionItems(CollectionModel<TransactionItemModel> transactionItems) {
        this.transactionItems = transactionItems;
    }
}
