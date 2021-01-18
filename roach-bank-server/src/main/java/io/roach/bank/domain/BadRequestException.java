package io.roach.bank.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a monetary transaction request
 * is illegal, i.e unbalanced or mixes currencies.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(message);
    }
}
