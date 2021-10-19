package io.roach.bank.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such transaction")
public class NoSuchTransactionException extends BusinessException {
    public NoSuchTransactionException(String id) {
        super("No such transaction: " + id);
    }
}
