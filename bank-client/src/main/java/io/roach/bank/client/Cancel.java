package io.roach.bank.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.support.AsyncHelper;
import io.roach.bank.client.support.CallMetrics;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Cancel extends AbstractCommand {
    @Autowired
    private AsyncHelper asyncHelper;

    @Autowired
    private CallMetrics callMetrics;

    @ShellMethod(value = "Cancel all workers", key = {"cancel", "x"})
    public void cancel() {
        callMetrics.clear();
        asyncHelper.cancelFutures();
    }
}
