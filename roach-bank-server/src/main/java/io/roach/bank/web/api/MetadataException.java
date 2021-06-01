package io.roach.bank.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.roach.bank.service.BusinessException;

@ResponseStatus(value = HttpStatus.FAILED_DEPENDENCY)
public class MetadataException extends BusinessException {
    public MetadataException() {
    }

    public MetadataException(String message) {
        super(message);
    }
}
