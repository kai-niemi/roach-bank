package io.roach.bank.web.api;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.AccountForm;
import io.roach.bank.api.AccountModel;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.web.support.FollowLocation;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/account")
public class AccountFormController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountResourceAssembler accountResourceAssembler;

    @Autowired
    private MetadataRepository metadataRepository;

    @GetMapping(value = "/form")
    public ResponseEntity<AccountForm> getAccountForm() {
        AccountForm form = new AccountForm();
        form.setUuid("auto");
        form.setName("A name");
        form.setDescription("A description");
        form.setRegion("stockholm");
        form.setCurrencyCode("SEK");
        form.setAccountType(AccountType.ASSET);

        Link link = Affordances.of(linkTo(methodOn(getClass()).getAccountForm()).withSelfRel()
                .andAffordance(afford(methodOn(getClass()).submitAccountForm(null))))
                .toLink();
        form.add(link);

        return ResponseEntity.ok(form);
    }

    @PostMapping(value = "/form")
    @TransactionBoundary
    public HttpEntity<AccountModel> submitAccountForm(@Valid @RequestBody AccountForm form) {
        UUID id = "auto".equals(form.getUuid()) ? UUID.randomUUID() : UUID.fromString(form.getUuid());

        Account account = Account.builder()
                .withId(id, form.getRegion())
                .withName(form.getName())
                .withDescription(form.getDescription())
                .withBalance(Money.of("0.00", form.getCurrencyCode()))
                .withAccountType(form.getAccountType())
                .withUpdated(LocalDateTime.now())
                .withAllowNegative(false)
                .build();

        account = accountRepository.createAccount(account);

        Link selfLink = linkTo(methodOn(AccountController.class)
                .getAccount(account.getUUID(), account.getRegion()))
                .withSelfRel();

        if (FollowLocation.ofCurrentRequest()) {
            return ResponseEntity.created(selfLink.toUri()).body(
                    accountResourceAssembler.toModel(account));
        } else {
            return ResponseEntity.created(selfLink.toUri()).build();
        }
    }

    @PostMapping(value = "/batch")
    @TransactionBoundary
    public HttpEntity<Void> submitAccountBatch(
            @RequestParam(value = "region", defaultValue = "") String region,
            @RequestParam(value = "prefix", defaultValue = "gen") String prefix,
            @RequestParam(value = "batchSize", defaultValue = "250") Integer batchSize
    ) {
        Currency currency = metadataRepository.getRegionCurrency(region);
        accountRepository.createAccountBatch(region, currency, sequence -> (prefix + "-" + sequence), batchSize);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
