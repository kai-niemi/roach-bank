package io.roach.bank.changefeed.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionChangeEvent extends AbstractPayload {
    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("transfer_date")
    private String transferDate;

    //    @JsonIgnore
    private Set<TransactionItemChangeEvent> items = new HashSet<>();

    public Set<TransactionItemChangeEvent> getItems() {
        return items;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(String transferDate) {
        this.transferDate = transferDate;
    }

    @Override
    public String toString() {
        return "TransactionChangeEvent{" +
                "transactionId" + transactionId +
                ", transactionType='" + transactionType + '\'' +
                ", transferDate='" + transferDate + '\'' +
                ", items=" + items +
                "} " + super.toString();
    }
}
