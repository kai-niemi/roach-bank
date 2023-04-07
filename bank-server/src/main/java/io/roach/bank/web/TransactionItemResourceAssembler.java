package io.roach.bank.web;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.TransactionItemModel;
import io.roach.bank.domain.TransactionItem;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class TransactionItemResourceAssembler
        extends RepresentationModelAssemblerSupport<TransactionItem, TransactionItemModel> {

    public TransactionItemResourceAssembler() {
        super(TransactionItemController.class, TransactionItemModel.class);
    }

    @Override
    public TransactionItemModel toModel(TransactionItem entity) {
        TransactionItemModel resource = new TransactionItemModel();
        resource.setAmount(entity.getAmount());
        resource.setNote(entity.getNote());

        resource.add(linkTo(TransactionItemController.class)
                .slash(entity.getId().getTransactionId())
                .slash(entity.getId().getAccountId())
                .withSelfRel());
        resource.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AccountController.class)
                        .getAccount(entity.getId().getAccountId()))
                .withRel(LinkRelations.ACCOUNT_REL)
                .withTitle("Booking account"));
        resource.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                        .getTransaction(entity.getId().getTransactionId()))
                .withRel(LinkRelations.TRANSACTION_REL)
                .withTitle("Booking transaction"));

        return resource;
    }

}
