package io.roach.bank.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such account")
public class NoSuchAccountException extends BusinessException {
    public NoSuchAccountException(UUID id) {
        super("No such account with id: " + id);
    }
}
