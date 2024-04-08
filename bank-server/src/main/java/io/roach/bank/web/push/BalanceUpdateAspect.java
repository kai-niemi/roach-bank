package io.roach.bank.web.push;

import io.roach.bank.AdvisorOrder;
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
public class BalanceUpdateAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BalancePublisher balancePublisher;

    @AfterReturning(pointcut = "execution(* io.roach.bank.service.DefaultTransactionService.createTransaction(..))",
            returning = "transaction")
    public void doAfterTransaction(Transaction transaction) {
        transaction.getItems().forEach(transactionItem -> {
            Account account = transactionItem.getAccount();

            AccountPayload payload = new AccountPayload();
            payload.setId(account.getId());
            payload.setName(account.getName());
            payload.setBalance(account.getBalance().getAmount());
            payload.setCurrency(account.getBalance().getCurrency().getCurrencyCode());
            payload.setCity(account.getCity());

            balancePublisher.publishAsync(payload);
        });
    }
}
