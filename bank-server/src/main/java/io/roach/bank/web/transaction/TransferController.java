package io.roach.bank.web.transaction;

import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.annotations.TransactionPriority;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.TransactionForm;
import io.roach.bank.api.TransactionModel;
import io.roach.bank.domain.Transaction;
import io.roach.bank.service.BadRequestException;
import io.roach.bank.service.TransactionService;
import io.roach.bank.web.support.FollowLocation;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/transfer")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TransferController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionResourceAssembler transactionResourceAssembler;

    @GetMapping("/form")
    public ResponseEntity<TransactionForm> getTransactionRequestForm() {
        TransactionForm.Builder formBuilder = TransactionForm.builder().withUUID(UUID.randomUUID())
                .withCity("city")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .withTransactionType("GEN");

        Link affordances = Affordances.of(
                        linkTo(methodOn(getClass())
                                .getTransactionRequestForm())
                                .withSelfRel()
                                .andAffordance(afford(methodOn(getClass()).submitTransactionForm(null))))
                .toLink();

        return ResponseEntity.ok().body(formBuilder.build().add(affordances));
    }

    @PostMapping(value = "/form")
    @TransactionBoundary(priority = TransactionPriority.NORMAL, retryPriority = TransactionPriority.HIGH)
    @Retryable
    public ResponseEntity<TransactionModel> submitTransactionForm(@Valid @RequestBody TransactionForm form) {
        if (form.getAccountLegs().size() % 2 != 0) {
            throw new BadRequestException("Account legs must be a multiple of 2: "
                    + form.getAccountLegs().size());
        }

        Link selfLink = linkTo(methodOn(TransactionController.class)
                .getTransaction(form.getUuid())).withSelfRel();

        Transaction entity = transactionService.createTransaction(form.getUuid(), form);

        if (FollowLocation.ofCurrentRequest()) {
            return ResponseEntity.created(selfLink.toUri())
                    .body(transactionResourceAssembler.toModel(entity));
        }

        return ResponseEntity.created(selfLink.toUri()).build();
    }
}
