package io.roach.bank.changefeed;

import javax.annotation.PostConstruct;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.roach.bank.ProfileNames;
import io.roach.bank.aspect.AdvisorOrder;
import io.roach.bank.domain.Transaction;

/**
 * Synthetic change feed publisher that mimics the end-to-end flow via CDC->Kafka->Websockets. Activated only if
 * kafka profile is not active.
 * <p>
 * Injected around the transactional createTransaction joinpoint and only invoked after transaction commit.
 */
@Aspect
@Component
@Order(AdvisorOrder.CHANGE_FEED_ADVISOR)
@Profile(ProfileNames.CDC_NONE)
public class FakeChangeFeedAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountChangeWebSocketPublisher changeFeedPublisher;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping AOP-driven (fake) change feed publisher");
    }

    @AfterReturning(pointcut = "execution(* io.roach.bank.service.DefaultTransactionService.createTransaction(..))", returning = "transaction")
    public void doAfterTransaction(Transaction transaction) {
        transaction.getItems().forEach(item -> {
            AccountPayload.Fields fields = new AccountPayload.Fields();
            fields.setId(item.getId().getAccountId());
            fields.setCity(item.getAccount().getCity());
            fields.setName(item.getAccount().getName());
            fields.setBalance(item.getRunningBalance().getAmount());
            fields.setCurrency(item.getRunningBalance().getCurrency().getCurrencyCode());

            AccountPayload changeEvent = new AccountPayload();
            changeEvent.setAfter(fields);
//            changeEvent.setUpdated(System.nanoTime() + ".0");

            changeFeedPublisher.publish(changeEvent);
        });
    }
}