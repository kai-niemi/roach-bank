package io.roach.bank.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FAILED_DEPENDENCY)
public class MetadataException extends RuntimeException {
    public MetadataException() {
    }

    public MetadataException(String message) {
        super(message);
    }
}
