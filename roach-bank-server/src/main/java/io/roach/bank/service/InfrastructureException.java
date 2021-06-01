package io.roach.bank.service;

/**
 * Base type for unrecoverable infrastructure exceptions.
 */
public class InfrastructureException extends RuntimeException {
    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
