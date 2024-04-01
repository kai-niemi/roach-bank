package io.roach.bank.client.command;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.TableModel;

import io.roach.bank.client.command.event.ClearErrorsEvent;
import io.roach.bank.client.command.event.ExecutionErrorEvent;
import io.roach.bank.client.command.support.TableUtils;

@ShellComponent
@ShellCommandGroup(Constants.LOGGING_COMMANDS)
public class Errors extends AbstractCommand {
    private static class Error {
        static Error from(ExecutionErrorEvent event) {
            Error e = new Error();
            e.instant = Instant.ofEpochMilli(event.getTimestamp());
            e.message = event.getMessage();
            e.cause = event.getCause();
            return e;
        }

        private Instant instant;

        private String message;

        private Throwable cause;
    }

    private final List<Error> errors = new LinkedList<>();

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @ShellMethod(value = "Print execution errors", key = {"print-errors", "pe"})
    public void printErrors(@ShellOption(defaultValue = "10") int limit) {
        console.success(TableUtils.prettyPrint(new TableModel() {
            @Override
            public int getRowCount() {
                return Math.min(limit, errors.size() + 1);
            }

            @Override
            public int getColumnCount() {
                return 4;
            }

            @Override
            public Object getValue(int row, int column) {
                if (row == 0) {
                    return List.of("#", "Time", "Message", "Cause").get(column);
                }

                Error e = errors.get(row - 1);

                switch (column) {
                    case 0 -> {
                        return row;
                    }
                    case 1 -> {
                        return e.instant;
                    }
                    case 2 -> {
                        return e.message;
                    }
                    case 3 -> {
                        return e.cause;
                    }
                    default -> {
                        return "??";
                    }
                }
            }
        }));

        if (errors.isEmpty()) {
            console.success("No errors");
        }
    }

    @ShellMethod(value = "Clear execution errors", key = {"clear-errors", "ce"})
    public void clearErrors() {
        errors.clear();
        applicationEventPublisher.publishEvent(new ClearErrorsEvent(this));
    }

//    @ShellMethod(key = {"fe"})
//    public void fakeError() {
//        applicationEventPublisher.publishEvent(new ExecutionErrorEvent(this,
//                "Error", new RuntimeException("Disturbance!")
//                .initCause(new IllegalStateException("1+1=3"))));
//    }

    @EventListener
    public void handle(ExecutionErrorEvent event) {
        if (errors.size() > 100) {
            errors.remove(0);
        }
        errors.add(Error.from(event));
    }
}
