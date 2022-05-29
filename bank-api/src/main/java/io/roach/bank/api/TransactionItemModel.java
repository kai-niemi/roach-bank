package io.roach.bank.api;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.roach.bank.api.support.Money;

import static io.roach.bank.api.LinkRelations.CURIE_PREFIX;

/**
 * Describes a transaction item leg resource representation.
 */
@Relation(value = CURIE_PREFIX + LinkRelations.TRANSACTION_ITEM_REL,
        collectionRelation = CURIE_PREFIX + LinkRelations.TRANSACTION_ITEMS_REL)
@JsonPropertyOrder({"links"})
public class TransactionItemModel extends RepresentationModel<TransactionItemModel> {
    private Money amount;

    private String note;

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
}
