package io.roach.bank.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.Availability;

public abstract class CommandSupport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public Availability connectedCheck() {
        return Connect.isConnected()
                ? Availability.available()
                : Availability.unavailable("You are not connected");
    }
}
