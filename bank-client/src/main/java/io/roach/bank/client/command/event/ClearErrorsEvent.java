package io.roach.bank.client.command.event;

import org.springframework.context.ApplicationEvent;

public class ClearErrorsEvent extends ApplicationEvent {
    public ClearErrorsEvent(Object source) {
        super(source);
    }
}
