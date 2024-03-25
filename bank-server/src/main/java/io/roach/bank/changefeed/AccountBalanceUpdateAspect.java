package io.roach.bank.changefeed;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.changefeed.model.AccountPayload;
import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(AdvisorOrder.OUTBOX_ADVISOR)
public class AccountBalanceUpdateAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WebSocketPublisher webSocketPublisher;

    @AfterReturning(pointcut = "execution(* io.roach.bank.service.DefaultTransactionService.createTransaction(..))",
            returning = "transaction")
    public void doAfterTransaction(Transaction transaction) {
        transaction.getItems()
                .forEach(transactionItem -> {
            Account account = transactionItem.getAccount();

            AccountPayload.Fields fields = new AccountPayload.Fields();
            fields.setId(account.getId());
            fields.setName(account.getName());
            fields.setBalance(account.getBalance().getAmount());
            fields.setCurrency(account.getBalance().getCurrency().getCurrencyCode());
            fields.setCity(account.getCity());

            AccountPayload payload = new AccountPayload();
            payload.setAfter(fields);

            webSocketPublisher.publishAsync(payload);
        });
    }
}
