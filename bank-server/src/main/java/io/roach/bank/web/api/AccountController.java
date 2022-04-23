package io.roach.bank.web.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TransactionNotAllowed;
import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.support.Money;
import io.roach.bank.domain.Account;
import io.roach.bank.service.AccountService;
import io.roach.bank.util.TimeBoundExecution;
import io.roach.bank.web.support.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/account")
@TransactionNotAllowed
public class AccountController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${roachbank.reportQueryTimeout}")
    private int queryTimeout;

    @Value("${roachbank.accountsPerCityLimit}")
    private int accountsPerCityLimit;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountResourceAssembler accountResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Account> pagedResourcesAssembler;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel("Monetary account resource");

        index.add(linkTo(methodOn(AccountController.class)
                .index()).withSelfRel());

        index.add(linkTo(methodOn(AccountController.class)
                .listAccounts(null, null, null))
                .withRel(LinkRelations.ACCOUNT_LIST_REL
                ).withTitle("Collection of accounts by page"));

        index.add(linkTo(methodOn(AccountController.class)
                .listAccounts(Collections.emptySet(), 0, 5))
                .withRel(LinkRelations.ACCOUNT_LIST_REL
                ).withTitle("First collection page of accounts"));

        index.add(linkTo(methodOn(AccountController.class)
                .listAccountsByCity(Collections.emptySet(), 10))
                .withRel(LinkRelations.ACCOUNT_TOP
                ).withTitle("Collection of top accounts grouped by region"));

        index.add(linkTo(methodOn(AccountFormController.class)
                .getAccountForm())
                .withRel(LinkRelations.ACCOUNT_FORM_REL
                ).withTitle("Form template for new account"));

        index.add(Link.of(UriTemplate.of(linkTo(AccountFormController.class)
                        .toUriComponentsBuilder().path(
                                "/batch/{?city,prefix,numAccounts,batchSize}")  // RFC-6570 template
                        .build().toUriString()),
                LinkRelations.ACCOUNT_BATCH_REL
        ).withTitle("Account creation batch"));

        return index;
    }

    @GetMapping(value = "/list")
    public PagedModel<AccountModel> listAccounts(
            @RequestParam(value = "cities", defaultValue = "", required = false) Set<String> cities,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "5", required = false) Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        return pagedResourcesAssembler
                .toModel(accountService.findAccountPage(cities, pageable), accountResourceAssembler);
    }

    @GetMapping(value = "/top")
    public ResponseEntity<CollectionModel<AccountModel>> listAccountsByCity(
            @RequestParam(value = "cities", defaultValue = "", required = false) Set<String> cities,
            @RequestParam(value = "limit", defaultValue = "-1", required = false) int limit
    ) {
        final List<Account> accounts = Collections.synchronizedList(new ArrayList<>());
        final int limitFinal = limit <= 0 ? this.accountsPerCityLimit : limit;

        // Retrieve accounts per region concurrently with a collective timeout
        List<Callable<Void>> tasks = new ArrayList<>();
        cities.forEach(city -> tasks.add(() -> {
            accounts.addAll(accountService.findAccountsByCity(city, limitFinal));
            return null;
        }));

        TimeBoundExecution.runConcurrently(tasks, queryTimeout, TimeUnit.SECONDS);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)) // Client-side caching
                .body(CollectionModel.of(accountResourceAssembler.toCollectionModel(accounts)));
    }

    @GetMapping(value = "/{id}")
    public AccountModel getAccount(
            @PathVariable("id") UUID id) {
        return accountResourceAssembler.toModel(accountService.getAccountById(id));
    }

    @GetMapping(value = "/{id}/balance")
    public Money getAccountBalance(
            @PathVariable("id") UUID id) {
        return accountService.getBalance(id);
    }

    @GetMapping(value = "/{id}/balance-snapshot")
    public Money getAccountBalanceSnapshot(
            @PathVariable("id") UUID id) {
        return accountService.getBalanceSnapshot(id);
    }

    @PutMapping(value = "/{id}/open")
    public AccountModel openAccount(
            @PathVariable("id") UUID id) {
        return accountResourceAssembler.toModel(accountService.openAccount(id));
    }

    @PutMapping(value = "/{id}/close")
    public AccountModel closeAccount(
            @PathVariable("id") UUID id) {
        return accountResourceAssembler.toModel(accountService.closeAccount(id));
    }
}

