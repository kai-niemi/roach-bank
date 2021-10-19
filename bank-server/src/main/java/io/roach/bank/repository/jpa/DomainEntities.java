package io.roach.bank.repository.jpa;

import io.roach.bank.domain.Account;
import io.roach.bank.domain.Transaction;
import io.roach.bank.domain.TransactionItem;

public abstract class DomainEntities {
    public static Class[] ENTITY_TYPES = {
            Account.class,
            Transaction.class,
            TransactionItem.class
    };

    private DomainEntities() {
    }
}
