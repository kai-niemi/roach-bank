package io.roach.bank.web.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TransactionNotAllowed;
import io.roach.bank.event.AccountChangeEvent;
import io.roach.bank.event.AccountChangePublisher;

@RestController
@RequestMapping(value = "/api/changefeed")
@Profile(ProfileNames.CDC_HTTP)
@TransactionNotAllowed
public class ChangeFeedController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountChangePublisher changeFeedPublisher;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping HTTP CDC-sink change feed publisher");
    }

    @PutMapping(value = "/account/{date}/{id}", consumes = {
            MediaType.ALL_VALUE
    })
    public ResponseEntity<Void> accountChangeEvent(
            @PathVariable(value = "date", required = false) String date,
            @PathVariable(value = "id", required = false) String id,
            @RequestBody String body) {
        try {
            List<AccountChangeEvent> changeEvents = new ArrayList<>();

            try (BufferedReader r = new BufferedReader(new StringReader(body))) {
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.startsWith("{") && line.endsWith("}")) {
                        AccountChangeEvent e = objectMapper.readerFor(AccountChangeEvent.class).readValue(line);
                        if (e.getResolved() == null) {
                            changeEvents.add(e);
                        }
                    }
                }
            }

            if (!changeEvents.isEmpty()) {
                logger.debug(
                        "accountChangeEvent ({}) received: date: {} id: {} payload bytes: {} event count (listing first 10):\n{}",
                        counter.incrementAndGet(), date, id, body.length(),
                        changeEvents.stream().limit(10).toArray());
                changeEvents.forEach(accountChangeEvent -> changeFeedPublisher.publish(accountChangeEvent));
            }
        } catch (IOException e) {
            logger.warn("accountChangeEvent received: date: {} id: {} body: {} processing error: {}",
                    date, id, body, e.getMessage());
            logger.warn("", e);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/transaction/{date}/{id}", consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Void> transactionChangeEvent(
            @PathVariable(value = "date", required = false) String date,
            @PathVariable(value = "id", required = false) String id,
            @RequestBody String body) {

        logger.debug("transactionChangeEvent ({}) received: date: {} id: {} body: {}",
                counter.incrementAndGet(), date, id, body);

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/transaction_item/{date}/{id}", consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Void> transactionItemChangeEvent(
            @PathVariable(value = "date", required = false) String date,
            @PathVariable(value = "id", required = false) String id,
            @RequestBody String body) {

        logger.debug("transactionItemChangeEvent ({}) received: date: {} id: {} body: {}",
                counter.incrementAndGet(), date, id, body);

        return ResponseEntity.ok().build();
    }
}
