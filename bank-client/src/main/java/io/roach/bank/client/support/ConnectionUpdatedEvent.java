package io.roach.bank.client.support;

import java.net.URI;

import org.springframework.context.ApplicationEvent;
import org.springframework.http.HttpStatus;

public class ConnectionUpdatedEvent extends ApplicationEvent {
    private URI baseUri;

    private HttpStatus httpStatus;

    public ConnectionUpdatedEvent(Object source, URI baseUri, HttpStatus httpStatus) {
        super(source);
        this.baseUri = baseUri;
        this.httpStatus = httpStatus;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
