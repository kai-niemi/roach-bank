package io.roach.bank.client.command;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.shell.Availability;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.client.support.ConnectionUpdatedEvent;
import io.roach.bank.client.support.ExecutorTemplate;
import io.roach.bank.client.support.TraversonHelper;

public abstract class RestCommandSupport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected TraversonHelper traversonHelper;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ExecutorTemplate executorTemplate;

    protected URI baseUri;

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.baseUri = event.getBaseUri();
    }

    public Availability connectedCheck() {
        return Connect.isConnected()
                ? Availability.available()
                : Availability.unavailable("You are not connected");
    }
}
