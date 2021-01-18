package io.roach.bank.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business exception thrown if an account has insufficient funds.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Negative balance")
public class NegativeBalanceException extends BadRequestException {
    public NegativeBalanceException(String accountName) {
        super("Insufficient funds for '" + accountName + "'");
    }
}
