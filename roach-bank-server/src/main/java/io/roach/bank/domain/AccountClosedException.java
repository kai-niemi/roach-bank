package io.roach.bank.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a closed account is referenced in a monetary transaction request.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Account closed")
public class AccountClosedException extends BusinessException {
    private String accountName;

    public AccountClosedException(String accountName) {
        super("Account is closed '" + accountName + "'");
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }
}
