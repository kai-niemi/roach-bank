package io.roach.bank.web;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.AccountStatus;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.domain.Account;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccountResourceAssembler
        extends RepresentationModelAssemblerSupport<Account, AccountModel> {
    public AccountResourceAssembler() {
        super(AccountController.class, AccountModel.class);
    }

    @Override
    public AccountModel toModel(Account entity) {
        AccountModel resource = new AccountModel();
        resource.setId(entity.getId());
        resource.setCity(entity.getCity());
        resource.setName(entity.getName());
        resource.setBalance(entity.getBalance());
        resource.setUpdatedAt(entity.getUpdatedAt());
        resource.setStatus(entity.isClosed() ? AccountStatus.CLOSED : AccountStatus.OPEN);
        resource.setAllowNegativeBalance(entity.getAllowNegative() > 0);
        resource.setDescription(entity.getDescription());
        resource.setAccountType(entity.getAccountType());

        resource.add(linkTo(methodOn(AccountController.class)
                .getAccount(entity.getId()))
                .withSelfRel());
        resource.add(linkTo(WebMvcLinkBuilder.methodOn(AccountController.class)
                .getAccountBalance(entity.getId()))
                .withRel(LinkRelations.ACCOUNT_BALANCE_REL)
                .withTitle("Account balance")
        );
        resource.add(linkTo(WebMvcLinkBuilder.methodOn(AccountController.class)
                .getAccountBalanceSnapshot(entity.getId()))
                .withRel(LinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                .withTitle("Account balance snapshot (follower read)")
        );

        if (entity.isClosed()) {
            resource.add(linkTo(methodOn(AccountController.class)
                    .openAccount(entity.getId())
            ).withRel(LinkRelations.OPEN_REL)
                    .withTitle("Open account"));
        } else {
            resource.add(linkTo(methodOn(AccountController.class)
                    .closeAccount(entity.getId())
            ).withRel(LinkRelations.CLOSE_REL)
                    .withTitle("Close account"));
        }

        return resource;
    }
}
