package io.roach.bank.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;

import io.roach.bank.client.support.Console;

public abstract class AbstractCommand {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected Console console;

    public Availability connectedCheck() {
        return Connect.isConnected()
                ? Availability.available()
                : Availability.unavailable("You are not connected");
    }
}
