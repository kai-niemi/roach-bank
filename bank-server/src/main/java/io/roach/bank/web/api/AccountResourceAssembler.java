package io.roach.bank.web.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.AccountStatus;
import io.roach.bank.api.BankLinkRelations;
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
        resource.setId(entity.getUUID());
        resource.setRegion(entity.getRegion());
        resource.setName(entity.getName());
        resource.setBalance(entity.getBalance());
        resource.setUpdated(entity.getUpdated());
        resource.setStatus(entity.isClosed() ? AccountStatus.CLOSED : AccountStatus.OPEN);
        resource.setAllowNegativeBalance(entity.getAllowNegative() > 0);
        resource.setDescription(entity.getDescription());
        resource.setAccountType(entity.getAccountType());

        resource.add(linkTo(methodOn(AccountController.class)
                .getAccount(entity.getUUID(), entity.getRegion()))
                .withSelfRel());
        resource.add(linkTo(WebMvcLinkBuilder.methodOn(AccountController.class)
                .getAccountBalance(entity.getUUID(), entity.getRegion()))
                .withRel(BankLinkRelations.ACCOUNT_BALANCE_REL)
                .withTitle("Account balance")
        );
        resource.add(linkTo(WebMvcLinkBuilder.methodOn(AccountController.class)
                .getAccountBalanceSnapshot(entity.getUUID(), entity.getRegion()))
                .withRel(BankLinkRelations.ACCOUNT_BALANCE_SNAPSHOT_REL)
                .withTitle("Account balance snapshot (follower read)")
        );

        if (entity.isClosed()) {
            resource.add(linkTo(methodOn(AccountController.class)
                    .openAccount(entity.getUUID(), entity.getRegion())
            ).withRel(BankLinkRelations.OPEN_REL)
                    .withTitle("Open account"));
        } else {
            resource.add(linkTo(methodOn(AccountController.class)
                    .closeAccount(entity.getUUID(), entity.getRegion())
            ).withRel(BankLinkRelations.CLOSE_REL)
                    .withTitle("Close account"));
        }

        return resource;
    }
}
