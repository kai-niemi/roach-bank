package io.roach.bank.web.push;

import java.util.ArrayList;
import java.util.List;
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
import io.roach.bank.web.account.AccountController;
import jakarta.annotation.PostConstruct;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BalancePublisher {
    private static final int BATCH_SIZE = 20;

    private final BlockingQueue<AccountPayload> payloadBuffer = new ArrayBlockingQueue<>(BATCH_SIZE);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    @Qualifier("clientOutboundChannelExecutor")
    private TaskExecutor taskScheduler;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter eventsQueued;

    private Counter eventsDropped;

    private Counter eventsSent;

    @PostConstruct
    public void init() {
        this.eventsQueued = Counter.builder("roach.bank.events.queued")
                .description("Events queued for STOMP publication")
                .register(meterRegistry);

        this.eventsDropped = Counter.builder("roach.bank.events.dropped")
                .description("Events dropped due to buffer overflow")
                .register(meterRegistry);

        this.eventsSent = Counter.builder("roach.bank.events.sent")
                .description("Events published over STOMP")
                .register(meterRegistry);

        // Drain events and push in batches
        taskScheduler.execute(payloadPublisher());
    }

    private Runnable payloadPublisher() {
        return () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    List<AccountPayload> payloadBatch = drainAccounts();
                    if (!payloadBatch.isEmpty()) {
                        simpMessagingTemplate.convertAndSend(TopicNames.TOPIC_ACCOUNT_UPDATE, payloadBatch);

                        eventsSent.increment(payloadBatch.size());

                        if (logger.isTraceEnabled()) {
                            logger.trace("Egress events: {} sent {} queued {} lost)",
                                    eventsSent.count(), eventsQueued.count(), eventsDropped.count());
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
        }

        payloads.forEach(payload -> {
            Link selfLink = linkTo(methodOn(AccountController.class)
                    .getAccount(payload.getId()))
                    .withSelfRel();
            payload.setHref(selfLink.getHref());
        });

        return payloads;
    }

    public void publishAsync(AccountPayload accountPayload) {
        if (accountPayload.getId() != null) {
            if (payloadBuffer.offer(accountPayload)) {
                eventsQueued.increment();
            } else {
                eventsDropped.increment();
            }
        }
    }
}
