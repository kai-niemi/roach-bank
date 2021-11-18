package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.ThrottledExecutor;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Cancel {
    @Autowired
    protected Console console;

    @Autowired
    private ThrottledExecutor throttledExecutor;

    @ShellMethod(value = "Cancel active workloads", key = {"cancel", "x"})
    public void cancel(@ShellOption(help = "worker name", defaultValue = "") String name) {
        if ("".equals(name)) {
            console.info("Cancelling all (%d) workers", throttledExecutor.activeWorkerCount());
            throttledExecutor.cancelAllWorkers();
        } else {
            console.info("Cancelling workers for '%s'", name);
            throttledExecutor.cancelWorkers(name);
        }
    }
}
