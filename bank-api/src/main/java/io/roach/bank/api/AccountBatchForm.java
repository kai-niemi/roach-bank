package io.roach.bank.api;


import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotNull;

import static io.roach.bank.api.LinkRelations.ACCOUNT_BATCH_FORM_REL;
import static io.roach.bank.api.LinkRelations.CURIE_PREFIX;

@Relation(value = CURIE_PREFIX + ACCOUNT_BATCH_FORM_REL)
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountBatchForm extends RepresentationModel<AccountBatchForm> {
    @NotNull
    private String city;

    @NotNull
    private String prefix;

    @NotNull
    private String balance;

    @NotNull
    private String currency;

    @NotNull
    private Integer batchSize;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
}
