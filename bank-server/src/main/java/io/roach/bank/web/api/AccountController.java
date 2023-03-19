package io.roach.bank.web.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.aspect.TimeTravelMode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import io.roach.bank.api.AccountBatchForm;
import io.roach.bank.api.AccountForm;
import io.roach.bank.api.AccountModel;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.service.AccountService;
import io.roach.bank.util.ConcurrencyUtils;
import io.roach.bank.web.support.FollowLocation;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/account")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AccountController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${roachbank.report-query-timeout}")
    private int reportQueryTimeoutSeconds;

    @Value("${roachbank.default-account-limit}")
    private int defaultAccountLimit;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountResourceAssembler accountResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Account> pagedResourcesAssembler;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel("Monetary account resource");

        index.add(linkTo(methodOn(getClass())
                .index()).withSelfRel());

        index.add(linkTo(methodOn(getClass())
                .listAccounts(null, null, null))
                .withRel(LinkRelations.ACCOUNT_LIST_REL
                ).withTitle("Collection of accounts"));

        index.add(linkTo(methodOn(getClass())
                .listTopAccounts(null, null))
                .withRel(LinkRelations.ACCOUNT_TOP
                ).withTitle("Collection of top accounts grouped by region"));

        index.add(linkTo(methodOn(getClass())
                .listTopAccounts(Collections.emptySet(), -1))
                .withRel(LinkRelations.ACCOUNT_TOP
                ).withTitle("Collection of top accounts grouped by region"));

        index.add(linkTo(methodOn(getClass())
                .getAccountForm())
                .withRel(LinkRelations.ACCOUNT_ONE_FORM_REL
                ).withTitle("Form template for one new account"));

        index.add(linkTo(methodOn(getClass())
                .getAccountBatchForm())
                .withRel(LinkRelations.ACCOUNT_BATCH_FORM_REL
                ).withTitle("Form template for a batch of new accounts"));

        return index;
    }

    @GetMapping(value = "/top")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public ResponseEntity<CollectionModel<AccountModel>> listTopAccounts(
            @RequestParam(value = "regions", defaultValue = "", required = false) Set<String> regions,
            @RequestParam(value = "limit", defaultValue = "-1", required = false) Integer limit
    ) {
        final Set<String> cities = metadataRepository.getRegionCities(regions);
        if (cities.isEmpty()) {
            logger.warn("No cities found matching regions [{}]", regions);
        }

        final int limitFinal = limit <= 0 ? this.defaultAccountLimit : limit;

        final List<Account> accounts = Collections.synchronizedList(new ArrayList<>());

        // Retrieve accounts per region concurrently with a collective timeout
        List<Callable<Void>> tasks = new ArrayList<>();
        cities.forEach(city -> tasks.add(() -> {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                accounts.addAll(accountService.findAccountsByCity(city, limitFinal));
            });
            return null;
        }));

        ConcurrencyUtils.runConcurrentlyAndWait(tasks, reportQueryTimeoutSeconds, TimeUnit.SECONDS);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)) // Client-side caching
                .body(CollectionModel.of(accountResourceAssembler.toCollectionModel(accounts)));
    }

    @GetMapping(value = "/list")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public PagedModel<AccountModel> listAccounts(
            @RequestParam(value = "regions", defaultValue = "", required = false) Set<String> regions,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "5", required = false) Integer size) {
        Set<String> cities = metadataRepository.getRegionCities(regions);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        return pagedResourcesAssembler
                .toModel(accountService.findAccountsByCity(cities, pageable), accountResourceAssembler);
    }

    @GetMapping(value = "/{id}")
    @TransactionBoundary(readOnly = true)
    @Retryable
    public AccountModel getAccount(@PathVariable("id") UUID id) {
        return accountResourceAssembler.toModel(accountService.getAccountById(id));
    }

    @GetMapping(value = "/{id}/balance")
    @TransactionBoundary(readOnly = true)
    @Retryable
    public Money getAccountBalance(@PathVariable("id") UUID id) {
        return accountService.getBalance(id);
    }

    @GetMapping(value = "/{id}/balance-snapshot")
    @TransactionBoundary(
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    @Retryable
    public Money getAccountBalanceSnapshot(@PathVariable("id") UUID id) {
        return accountService.getBalanceSnapshot(id);
    }

    @PutMapping(value = "/{id}/open")
    @TransactionBoundary
    public AccountModel openAccount(@PathVariable("id") UUID id) {
        return accountResourceAssembler.toModel(accountService.openAccount(id));
    }

    @PutMapping(value = "/{id}/close")
    @TransactionBoundary
    public AccountModel closeAccount(@PathVariable("id") UUID id) {
        return accountResourceAssembler.toModel(accountService.closeAccount(id));
    }

    @GetMapping(value = "/one/form")
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

    @PostMapping(value = "/one/form")
    @TransactionBoundary
    public HttpEntity<AccountModel> createOneAccount(@Valid @RequestBody AccountForm form) {
        UUID id = "auto".equals(form.getUuid()) ? UUID.randomUUID() : UUID.fromString(form.getUuid());

        Account account = Account.builder()
                .withId(id)
                .withName(form.getName())
                .withCity(form.getCity())
                .withDescription(form.getDescription())
                .withBalance(Money.of("0.00", form.getCurrencyCode()))
                .withAccountType(form.getAccountType())
                .withAllowNegative(false)
                .build();

        accountService.createAccount(account);

        Link selfLink = linkTo(methodOn(getClass())
                .getAccount(account.getId()))
                .withSelfRel();

        if (FollowLocation.ofCurrentRequest()) {
            return ResponseEntity.created(selfLink.toUri()).body(accountResourceAssembler.toModel(account));
        } else {
            return ResponseEntity.created(selfLink.toUri()).build();
        }
    }

    @GetMapping(value = "/batch/form")
    public ResponseEntity<AccountBatchForm> getAccountBatchForm() {
        AccountBatchForm form = new AccountBatchForm();
        form.setCity("");
        form.setPrefix("");
        form.setBalance("50000.00");
        form.setCurrency("USD");
        form.setNumAccounts(1024);
        form.setBatchSize(128);

        form.add(Affordances.of(linkTo(methodOn(getClass()).getAccountBatchForm()).withSelfRel()
                        .andAffordance(afford(methodOn(getClass()).createBatchAccounts(null))))
                .toLink());

        return ResponseEntity.ok(form);
    }

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    @PostMapping(value = "/batch/form")
    @TransactionBoundary
    public HttpEntity<?> createBatchAccounts(@Valid @RequestBody AccountBatchForm form) {
        final Money money = Money.of(form.getBalance(), form.getCurrency());

        List<UUID> ids = accountService.createAccountBatch(() -> Account.builder()
                        .withId(UUID.randomUUID())
                        .withName(String.format("%s%05d", form.getPrefix(), SEQ.incrementAndGet()))
                        .withDescription(CockroachFacts.nextFact(256))
                        .withCity(form.getCity())
                        .withBalance(money)
                        .withAccountType(AccountType.ASSET)
                        .build(),
                form.getNumAccounts(), form.getBatchSize());

        List<Link> links = new ArrayList<>();
        ids.forEach(id -> links.add(linkTo(methodOn(getClass())
                .getAccount(id))
                .withSelfRel()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityModel.of(form, links));
    }
}

