package io.roach.bank.client.command.event;

import java.net.URI;

import org.springframework.context.ApplicationEvent;
import org.springframework.http.HttpStatusCode;

public class ConnectionUpdatedEvent extends ApplicationEvent {
    private URI baseUri;

    private HttpStatusCode httpStatus;

    public ConnectionUpdatedEvent(Object source, URI baseUri, HttpStatusCode httpStatus) {
        super(source);
        this.baseUri = baseUri;
        this.httpStatus = httpStatus;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public HttpStatusCode getHttpStatus() {
        return httpStatus;
    }
}
