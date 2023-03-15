package io.roach.bank.changefeed.egress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.hateoas.Link;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.roach.bank.changefeed.model.AccountPayload;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;
import io.roach.bank.web.api.AccountController;
import jakarta.annotation.PostConstruct;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class AccountChangeWebSocketPublisher {
    private static final int BATCH_SIZE = 20;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlockingQueue<AccountPayload> payloadBuffer = new ArrayBlockingQueue<>(BATCH_SIZE);

    private final BlockingQueue<TransactionItem> transactionBuffer = new ArrayBlockingQueue<>(BATCH_SIZE);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    @Qualifier("clientOutboundChannelExecutor")
    private TaskExecutor taskScheduler;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter eventsQueued;

    private Counter eventsLost;

    private Counter eventsSent;

    @PostConstruct
    public void init() {
        eventsQueued = meterRegistry.counter("bank.events.queued");
        eventsLost = meterRegistry.counter("bank.events.lost");
        eventsSent = meterRegistry.counter("bank.events.sent");

        // Drain events and push in batches
        taskScheduler.execute(payloadPublisher());
    }

    private Runnable payloadPublisher() {
        return () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    List<AccountPayload> payloadBatch = drainAccounts();
                    if (!payloadBatch.isEmpty()) {
                        simpMessagingTemplate.convertAndSend("/topic/accounts", payloadBatch);

                        eventsSent.increment(payloadBatch.size());

                        if (logger.isTraceEnabled()) {
                            logger.trace("Egress events: {} sent {} queued {} lost)",
                                    eventsSent.count(), eventsQueued.count(), eventsLost.count());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("", e);
            }
        };
    }

    private List<AccountPayload> drainAccounts() throws InterruptedException {
        final List<AccountPayload> payloads = new ArrayList<>();

        AccountPayload accountPayload = payloadBuffer.poll(500, TimeUnit.MILLISECONDS);
        if (accountPayload != null) {
            payloads.add(accountPayload);
            payloadBuffer.drainTo(payloads, BATCH_SIZE);
        } else {
            TransactionItem transactionItem = transactionBuffer.poll(500, TimeUnit.MILLISECONDS);
            if (transactionItem != null) {
                Set<TransactionItem> ids = new HashSet<>();
                ids.add(transactionItem);
                transactionBuffer.drainTo(ids, BATCH_SIZE);

                ids.forEach(item -> {
                    Account account = item.getAccount();

                    AccountPayload.Fields fields = new AccountPayload.Fields();
                    fields.setId(account.getId());
                    fields.setCity(item.getCity());
                    fields.setName(account.getName());
                    fields.setBalance(account.getBalance().getAmount());
                    fields.setCurrency(account.getBalance().getCurrency().getCurrencyCode());

                    AccountPayload payload = new AccountPayload();
                    payload.setAfter(fields);

                    payloads.add(payload);
                });
            }
        }

        payloads.forEach(payload -> {
            Link selfLink = linkTo(methodOn(AccountController.class)
                    .getAccount(payload.getId()))
                    .withSelfRel();
            payload.setHref(selfLink.getHref());
        });

        return payloads;
    }

    public void publish(Transaction transaction) {
        transaction.getItems().forEach(transactionItem -> {
            if (transactionBuffer.offer(Objects.requireNonNull(transactionItem))) {
                eventsQueued.increment();
            } else {
                eventsLost.increment();
            }
        });
    }

    public void publish(AccountPayload accountPayload) {
        if (accountPayload.getId() != null) {
            if (payloadBuffer.offer(accountPayload)) {
                eventsQueued.increment();
            } else {
                eventsLost.increment();
            }
        }
    }
}
