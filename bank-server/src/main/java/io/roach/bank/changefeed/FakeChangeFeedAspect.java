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
import io.roach.bank.AdvisorOrder;
import io.roach.bank.changefeed.egress.AccountChangeWebSocketPublisher;
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
        changeFeedPublisher.publish(transaction);
    }
}
