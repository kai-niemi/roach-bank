package io.roach.bank.web.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.api.support.CockroachFacts;
import io.roach.bank.api.support.Money;
import io.roach.bank.api.support.RandomData;
import io.roach.bank.domain.Account;
import io.roach.bank.service.BadRequestException;
import io.roach.bank.domain.Transaction;
import io.roach.bank.repository.AccountRepository;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.service.BankService;
import io.roach.bank.web.support.FollowLocation;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/transaction")
public class TransactionFormController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BankService bankService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private TransactionResourceAssembler transactionResourceAssembler;

    @GetMapping("/form")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public ResponseEntity<TransactionForm> getTransactionRequestForm(
            @RequestParam(value = "accountsPerRegion", defaultValue = "2", required = false) int accountsPerRegion,
            @RequestParam(value = "amount", defaultValue = "5.00", required = false) final String amount,
            @RequestParam(value = "regions", required = false, defaultValue = "") List<String> regions) {

        if (accountsPerRegion < 2) {
            throw new BadRequestException("Accounts per region must be >= 2: " + accountsPerRegion);
        }
        if (accountsPerRegion % 2 != 0) {
            throw new BadRequestException("Accounts per region must be a multiple of 2: " + accountsPerRegion);
        }
        if (regions.isEmpty()) {
            regions = Collections.singletonList(RandomData.selectRandom(metadataRepository.getLocalRegions()));
        }

        List<Account> accounts = new ArrayList<>();

        metadataRepository.resolveRegions(regions).keySet().forEach(
                r -> accounts.addAll(accountRepository.findAccountsByRegion(r, accountsPerRegion)));

        if (accounts.isEmpty()) {
            throw new MetadataException("No accounts matching regions: "
                    + StringUtils.collectionToCommaDelimitedString(regions));
        }

        TransactionForm.Builder formBuilder = TransactionForm.builder()
                .withUUID("auto")
                .withRegion(accounts.iterator().next().getRegion())
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .withTransactionType("GEN");

        AtomicBoolean flip = new AtomicBoolean();

        accounts.forEach(account -> {
            Money a = Money.of(amount, account.getBalance().getCurrency());
            if (flip.getAndSet(!flip.get())) {
                a = a.negate();
            }
            formBuilder.addLeg()
                    .withId(account.getUUID(), account.getRegion())
                    .withAmount(a)
                    .withNote(CockroachFacts.nextFact())
                    .then();
        });


        Link affordances = Affordances.of(linkTo(methodOn(getClass())
                .getTransactionRequestForm(accountsPerRegion, amount, regions))
                .withSelfRel()
                .andAffordance(afford(methodOn(getClass())
                        .submitTransactionForm(null))))
                .toLink();

        return ResponseEntity.ok().body(formBuilder.build().add(affordances));
    }

    @PostMapping(value = "/form")
    @TransactionBoundary
    public ResponseEntity<TransactionModel> submitTransactionForm(@Valid @RequestBody TransactionForm form) {
        UUID uuid = "auto".equals(form.getUuid()) ? UUID.randomUUID() : UUID.fromString(form.getUuid());
        Transaction.Id id = Transaction.Id.of(uuid, form.getRegion());

        try {
            Link selfLink = linkTo(methodOn(TransactionController.class)
                    .getTransaction(id.getUUID(), id.getRegion()))
                    .withSelfRel();
            Transaction entity = bankService.createTransaction(id, form);
            if (FollowLocation.ofCurrentRequest()) {
                return ResponseEntity.created(selfLink.toUri()).body(transactionResourceAssembler.toModel(entity));
            }
            return ResponseEntity.created(selfLink.toUri()).build();
        } catch (DataIntegrityViolationException e) {
            String msg = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
            if (msg.contains("duplicate key value")) {
                logger.warn("Duplicate transaction request: {}", id);
                return ResponseEntity.status(HttpStatus.OK)
                        .location(linkTo(methodOn(TransactionController.class)
                                .getTransaction(id.getUUID(), id.getRegion()))
                                .toUri())
                        .build();
            }
            throw e;
        }
    }

}
