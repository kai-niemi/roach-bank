package io.roach.bank.web.api;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.roach.bank.ProfileNames;
import io.roach.bank.changefeed.egress.AccountChangeWebSocketPublisher;
import io.roach.bank.changefeed.model.AccountPayload;
import io.roach.bank.changefeed.model.ChangeFeedEvent;

@RestController
@RequestMapping(value = "/api/cdc/webhook")
@Profile(ProfileNames.CDC_HTTP)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ChangeFeedController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountChangeWebSocketPublisher changeFeedPublisher;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping webhook CDC-sink changefeed dispatcher");
    }

    @PostMapping(value = "/account", consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Void> accountChangeEvent(@RequestBody String body) {
        logger.debug("accountChangeEvent ({}): {}", counter.incrementAndGet(), body);

        try {
            ChangeFeedEvent<AccountPayload> event = objectMapper.readerFor(ChangeFeedEvent.class)
                    .readValue(body);
            if (!StringUtils.hasLength(event.getResolved())) {
                event.getPayload().forEach(accountPayload -> changeFeedPublisher.publish(accountPayload));
            }
        } catch (IOException e) {
            logger.warn("", e);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/transaction", consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Void> transactionChangeEvent(@RequestBody String body) {
        logger.debug("transactionChangeEvent ({}): {}", counter.incrementAndGet(), body);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/transaction_item", consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Void> transactionItemChangeEvent(@RequestBody String body) {
        logger.debug("transactionItemChangeEvent ({}): {}", counter.incrementAndGet(), body);
        return ResponseEntity.ok().build();
    }
}
