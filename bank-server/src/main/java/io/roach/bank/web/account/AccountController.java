package io.roach.bank.web.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.springframework.data.cockroachdb.annotations.TimeTravelMode;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.AccountBatchForm;
import io.roach.bank.api.AccountForm;
import io.roach.bank.api.AccountModel;
import io.roach.bank.api.AccountType;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.repository.RegionRepository;
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

    @Value("${roachbank.default-account-limit}")
    private int defaultAccountLimit;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountResourceAssembler accountResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Account> pagedResourcesAssembler;

    @Autowired
    private RegionRepository metadataRepository;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel("Monetary account resource");

        index.add(linkTo(methodOn(getClass())
                .index()).withSelfRel());

        index.add(linkTo(methodOn(getClass())
                .listAccounts(null, null))
                .withRel(LinkRelations.ACCOUNT_LIST_REL
                ).withTitle("Collection of accounts"));

        index.add(linkTo(methodOn(getClass())
                .listTopAccounts(null, null))
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
    public ResponseEntity<CollectionModel<AccountModel>> listTopAccounts(
            @RequestParam(value = "region", defaultValue = "all", required = false) String region,
            @RequestParam(value = "limit", defaultValue = "-1", required = false) Integer limit
    ) {
        final int finalLimit = limit <= 0 ? this.defaultAccountLimit : limit;

        List<String> regions = "gateway".equals(region)
                ? List.of(metadataRepository.getGatewayRegion())
                : "all".equals(region)
                ? List.of() : List.of(region);

        final List<Callable<List<Account>>> tasks = new ArrayList<>();

        metadataRepository.listRegions(regions).forEach(r -> tasks.add(() -> {
            Collection<String> cities = metadataRepository.listCities(List.of(r));
            List<Account> accounts = accountService.findTopAccountsByCity(cities, finalLimit);
            if (logger.isDebugEnabled()) {
                accounts.stream()
                        .limit(10)
                        .forEach(account -> {
                            logger.debug("%s %s %s %s".formatted(
                                    account.getId(), account.getName(), account.getBalance(), account.getCity()));
                        });
                logger.debug("Cities [%s] limit [%d]".formatted(cities, finalLimit));
            }
            return accounts;
        }));

        final List<Account> availableAccounts = new ArrayList<>();

        // Retrieve accounts per region concurrently with a collective timeout
        int completions = ConcurrencyUtils.runConcurrentlyAndWait(tasks,
                5, TimeUnit.SECONDS, availableAccounts::addAll);

        logger.warn("Completed %d of %d account retrieval tasks"
                .formatted(completions, tasks.size()));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES)) // Client-side caching
                .body(accountResourceAssembler.toCollectionModel(availableAccounts));
    }

    @GetMapping(value = "/all")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ), readOnly = true)
    public PagedModel<AccountModel> listAccounts(
            @RequestParam(value = "region", defaultValue = "all", required = false) String region,
            @PageableDefault(size = 30) Pageable page) {
        List<String> regions = "gateway".equals(region)
                ? List.of(metadataRepository.getGatewayRegion())
                : "all".equals(region)
                ? List.of() : List.of(region);

        Collection<String> cities = metadataRepository.listCities(metadataRepository.listRegions(regions));
        logger.info("Found [{}] in regions [{}]", cities, regions);

        return pagedResourcesAssembler
                .toModel(accountService.findAll(cities, page), accountResourceAssembler);
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
        form.setBatchSize(128);

        form.add(Affordances.of(linkTo(methodOn(getClass()).getAccountBatchForm()).withSelfRel()
                        .andAffordance(afford(methodOn(getClass()).createAccountBatch(null))))
                .toLink());

        return ResponseEntity.ok(form);
    }

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    @PostMapping(value = "/batch/form")
    @TransactionBoundary
    public HttpEntity<?> createAccountBatch(@Valid @RequestBody AccountBatchForm form) {
        final Money money = Money.of(form.getBalance(), form.getCurrency());

        List<UUID> ids = accountService.createAccountBatch(() -> Account.builder()
                        .withId(UUID.randomUUID())
                        .withName(String.format("%s%05d", form.getPrefix(), SEQ.incrementAndGet()))
                        .withDescription(CockroachFacts.nextFact(256))
                        .withCity(form.getCity())
                        .withBalance(money)
                        .withAccountType(AccountType.ASSET)
                        .build(),
                form.getBatchSize());

        List<Link> links = new ArrayList<>();
        ids.forEach(id -> links.add(linkTo(methodOn(getClass())
                .getAccount(id))
                .withSelfRel()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityModel.of(form, links));
    }
}

