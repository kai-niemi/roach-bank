package io.roach.bank.client.command.event;

import org.springframework.context.ApplicationEvent;

public class ExecutionErrorEvent extends ApplicationEvent {
    private final String message;

    private final Throwable cause;

    public ExecutionErrorEvent(Object source, String message, Throwable cause) {
        super(source);
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
