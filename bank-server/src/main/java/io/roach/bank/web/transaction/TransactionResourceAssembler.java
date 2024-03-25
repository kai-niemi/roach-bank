package io.roach.bank.web.transaction;

import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.server.core.DummyInvocationUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.domain.Transaction;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TransactionResourceAssembler
        extends RepresentationModelAssemblerSupport<Transaction, TransactionModel> {

    public TransactionResourceAssembler() {
        super(TransactionController.class, TransactionModel.class);
    }

    @Override
    public TransactionModel toModel(Transaction entity) {
        TransactionModel resource = new TransactionModel();
        resource.add(linkTo(methodOn(TransactionController.class)
                .getTransaction(entity.getId())).withSelfRel());

        resource.setTransactionId(entity.getId());
        resource.setTransactionType(entity.getTransactionType());
        resource.setBookingDate(entity.getBookingDate());
        resource.setTransactionDate(entity.getTransferDate());

        resource.add(linkTo(DummyInvocationUtils.methodOn(TransactionItemController.class)
                .getTransactionItems(entity.getId(),
                        PageRequest.of(0, 5)))
                .withRel(LinkRelations.TRANSACTION_ITEMS_REL)
                .withTitle("Transaction items"));

        return resource;
    }
}
