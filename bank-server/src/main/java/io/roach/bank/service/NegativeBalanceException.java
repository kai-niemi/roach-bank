package io.roach.bank.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business exception thrown if an account has insufficient funds.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Negative balance violation")
public class NegativeBalanceException extends BadRequestException {
    public NegativeBalanceException(String message) {
        super(message);
    }

    public NegativeBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
