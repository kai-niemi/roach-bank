package io.roach.bank.changefeed.egress;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.ProfileNames;
import io.roach.bank.changefeed.model.AccountPayload;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import jakarta.annotation.PostConstruct;

@Aspect
@Component
@Order(AdvisorOrder.CHANGE_FEED_ADVISOR)
@Profile(ProfileNames.CDC_NONE)
public class ChangeFeedAdapterAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WebSocketPublisher changeFeedPublisher;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping AOP-driven change feed publisher");
    }

    @AfterReturning(pointcut = "execution(* io.roach.bank.service.DefaultTransactionService.createTransaction(..))",
            returning = "transaction")
    public void doAfterTransaction(Transaction transaction) {
        transaction.getItems().forEach(transactionItem -> {
            Account account = transactionItem.getAccount();
            if (!account.isByReference()) {
                AccountPayload.Fields fields = new AccountPayload.Fields();
                fields.setId(account.getId());
                fields.setName(account.getName());
                fields.setBalance(account.getBalance().getAmount());
                fields.setCurrency(account.getBalance().getCurrency().getCurrencyCode());
                fields.setCity(transactionItem.getCity());

                AccountPayload payload = new AccountPayload();
                payload.setAfter(fields);

                changeFeedPublisher.publish(payload);
            }
        });
    }
}
