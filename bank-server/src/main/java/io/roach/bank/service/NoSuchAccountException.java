package io.roach.bank.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such account")
public class NoSuchAccountException extends BusinessException {
    public NoSuchAccountException(String name) {
        super("No such account: " + name);
    }
}
