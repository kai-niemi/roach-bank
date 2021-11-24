package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.support.BoundedExecutor;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Cancel {
    @Autowired
    private BoundedExecutor boundedExecutor;

    @ShellMethod(value = "Cancel active workloads", key = {"cancel", "x"})
    public void cancel() {
        boundedExecutor.cancelAndRestart();
    }
}
