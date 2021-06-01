package io.roach.bank.push;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.hateoas.Link;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.roach.bank.web.api.AccountController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class AccountChangeWebSocketPublisher {
    private static final int BATCH_SIZE = 20;

    private final BlockingQueue<AccountChangeEvent> buffer = new ArrayBlockingQueue<>(BATCH_SIZE);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${roachbank.pushPermitsPerSec}")
    private double permitsPerSec;

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

        // Outbound rate limiter
        final RateLimiter rateLimiter = RateLimiter.create(permitsPerSec);

        // Drain events and push in batches
        taskScheduler.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    rateLimiter.acquire();

                    final AccountChangeEvent changeEvent = buffer.poll(1000, TimeUnit.MILLISECONDS);
                    if (changeEvent != null) {
                        List<AccountChangeEvent> batch = new ArrayList<>();
                        batch.add(changeEvent);

                        buffer.drainTo(batch, BATCH_SIZE / 2);

                        simpMessagingTemplate.convertAndSend("/topic/accounts", batch);
                        eventsSent.increment(batch.size());

                        if (logger.isTraceEnabled()) {
                            logger.trace("Events ({} sent, {} queued, {} lost)",
                                    eventsSent.count(), eventsQueued.count(), eventsLost.count());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void publish(AccountChangeEvent changeEvent) {
        if (changeEvent.getId() != null) {
            Link selfLink = linkTo(methodOn(AccountController.class)
                    .getAccount(changeEvent.getId(), changeEvent.getRegion()))
                    .withSelfRel();
            changeEvent.setHref(selfLink.getHref());

            if (buffer.offer(changeEvent)) {
                eventsQueued.increment();
            } else {
                eventsLost.increment();
            }
        }
    }
}
