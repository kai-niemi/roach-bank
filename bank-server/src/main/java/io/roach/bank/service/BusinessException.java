package io.roach.bank.service;

/**
 * Base type for unrecoverable business exceptions.
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException() {
    }

    protected BusinessException(String message) {
        super(message);
    }
}
