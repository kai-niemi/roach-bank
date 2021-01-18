package io.roach.bank.web.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import io.roach.bank.api.BankLinkRelations;
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
        resource.setRunningBalance(entity.getRunningBalance());
        resource.setNote(entity.getNote());

        resource.add(linkTo(TransactionItemController.class)
                .slash(entity.getId().getTransactionId())
                .slash(entity.getId().getTransactionRegion())
                .slash(entity.getId().getAccountId())
                .slash(entity.getId().getAccountRegion())
                .withSelfRel());
        resource.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AccountController.class)
                        .getAccount(entity.getAccount().getUUID(), entity.getAccount().getRegion()))
                .withRel(BankLinkRelations.ACCOUNT_REL)
                .withTitle("Booking account"));
        resource.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                        .getTransaction(entity.getTransaction().getUUID(),
                                entity.getTransaction().getRegion()))
                .withRel(BankLinkRelations.TRANSACTION_REL)
                .withTitle("Booking transaction"));

        return resource;
    }

}
