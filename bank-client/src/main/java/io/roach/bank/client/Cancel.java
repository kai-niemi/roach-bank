package io.roach.bank.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.support.CallMetrics;
import io.roach.bank.client.support.ExecutorTemplate;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Cancel extends AbstractCommand {
    @Autowired
    private ExecutorTemplate executorTemplate;

    @Autowired
    private CallMetrics callMetrics;

    @ShellMethod(value = "Cancel all workloads", key = {"cancel", "x"})
    public void cancel() {
        executorTemplate.cancelFutures();
        callMetrics.clear();
    }
}
