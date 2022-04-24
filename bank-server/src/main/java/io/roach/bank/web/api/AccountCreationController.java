package io.roach.bank.web.api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
public class AccountCreationController {
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
        form.setCity("stockholm");
        form.setCurrencyCode("SEK");
        form.setAccountType(AccountType.ASSET);

        form.add(Affordances.of(linkTo(methodOn(getClass()).getAccountForm()).withSelfRel()
                        .andAffordance(afford(methodOn(getClass()).submitAccountForm(null))))
                .toLink());

        return ResponseEntity.ok(form);
    }

    @PostMapping(value = "/form")
    @TransactionBoundary
    public HttpEntity<AccountModel> submitAccountForm(@Valid @RequestBody AccountForm form) {
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

    @PostMapping(value = "/batch")
    @TransactionBoundary
    public HttpEntity<Void> submitAccountBatch(
            @RequestParam(value = "city", defaultValue = "") String city,
            @RequestParam(value = "prefix", defaultValue = "rnd") String prefix,
            @RequestParam(value = "balance", defaultValue = "100000.00") String balance,
            @RequestParam(value = "numAccounts", defaultValue = "1024") Integer numAccounts,
            @RequestParam(value = "batchSize", defaultValue = "32") Integer batchSize
    ) {
        Map<String, Currency> cities = metadataRepository.getCities();
        if (!cities.containsKey(city)) {
            throw new MetadataException("No such city: " + city);
        }

        final Instant startTime = Instant.now();
        logger.info("Creating {} accounts in city {} using batch size {}", numAccounts, city, batchSize);

        final Currency currency = cities.get(city);
        final Money money = Money.of(balance, currency);
        final AtomicInteger sequence = new AtomicInteger(1);

        accountRepository.createAccounts(() -> Account.builder()
                        .withId(UUID.randomUUID())
                        .withName(prefix + "-" + sequence.incrementAndGet())
                        .withCity(city)
                        .withBalance(money)
                        .withAccountType(AccountType.ASSET)
                        .build(),
                numAccounts, batchSize);

        logger.info("Created {} accounts in city {} using batch size {} in {}",
                numAccounts, city, batchSize, Instant.now().minusMillis(startTime.toEpochMilli()).toString());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}