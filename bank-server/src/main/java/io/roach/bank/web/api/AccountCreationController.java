package io.roach.bank.web.api;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
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

import io.roach.bank.api.AccountForm;
import io.roach.bank.api.AccountModel;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.web.support.FollowLocation;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/account")
public class AccountCreationController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountResourceAssembler accountResourceAssembler;

    @GetMapping(value = "/form")
    public ResponseEntity<AccountForm> getAccountForm() {
        AccountForm form = new AccountForm();
        form.setUuid("auto");
        form.setName("A name");
        form.setDescription("A description");
        form.setCity("stockholm");
        form.setCurrencyCode("SEK");
        form.setAccountType(AccountType.ASSET);

        form.add(Affordances.of(linkTo(methodOn(getClass()).getAccountForm()).withSelfRel()
                        .andAffordance(afford(methodOn(getClass()).createOneAccount(null))))
                .toLink());

        return ResponseEntity.ok(form);
    }

    @PostMapping(value = "/form")
    @TransactionBoundary
    public HttpEntity<AccountModel> createOneAccount(@Valid @RequestBody AccountForm form) {
        UUID id = "auto".equals(form.getUuid()) ? UUID.randomUUID() : UUID.fromString(form.getUuid());

        Account account = Account.builder()
                .withId(id)
                .withName(form.getName())
                .withDescription(form.getDescription())
                .withBalance(Money.of("0.00", form.getCurrencyCode()))
                .withAccountType(form.getAccountType())
                .withUpdated(LocalDateTime.now())
                .withAllowNegative(false)
                .build();

        account = accountRepository.createAccount(account);

        Link selfLink = linkTo(methodOn(AccountController.class)
                .getAccount(account.getId()))
                .withSelfRel();

        if (FollowLocation.ofCurrentRequest()) {
            return ResponseEntity.created(selfLink.toUri()).body(
                    accountResourceAssembler.toModel(account));
        } else {
            return ResponseEntity.created(selfLink.toUri()).build();
        }
    }

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    @PostMapping(value = "/batch")
    public HttpEntity<Void> createBatchAccounts(
            @RequestParam(value = "city", defaultValue = "") String city,
            @RequestParam(value = "prefix", defaultValue = "") String prefix,
            @RequestParam(value = "balance", defaultValue = "50000.00") String balance,
            @RequestParam(value = "currency", defaultValue = "USD") String currency,
            @RequestParam(value = "numAccounts", defaultValue = "1024") Integer numAccounts,
            @RequestParam(value = "batchSize", defaultValue = "64") Integer batchSize
    ) {
        final Money money = Money.of(balance, currency);

        final long startTime = System.currentTimeMillis();

        accountRepository.createAccounts(() -> Account.builder()
                        .withId(UUID.randomUUID())
                        .withName(String.format("%s%05d", prefix, SEQ.incrementAndGet()))
                        .withDescription(CockroachFacts.nextFact(256))
                        .withCity(city)
                        .withBalance(money)
                        .withAccountType(AccountType.ASSET)
                        .build(),
                numAccounts, batchSize);

        logger.info("Created {} accounts in '{}' using batch size {} in {} ms",
                numAccounts, city, batchSize, System.currentTimeMillis() - startTime);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
