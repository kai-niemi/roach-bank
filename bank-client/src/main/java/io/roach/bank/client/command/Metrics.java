package io.roach.bank.client.command;

import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.client.support.CallStats;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.SchedulingHelper;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Metrics {
    public static final int PERIOD_SECONDS = 5;

    private static int PRINT_INTERVAL = PERIOD_SECONDS;

    private ScheduledFuture<?> statsFuture;

    @Autowired
    protected Console console;

    @Autowired
    private SchedulingHelper scheduler;

    @PostConstruct
    protected void init() {
        this.statsFuture = scheduler.scheduleAtFixedRate(CallStats::printStdOut, PRINT_INTERVAL, PRINT_INTERVAL);
    }

    @ShellMethod(value = "Frequency for printing per-operation metrics (default is 5s)", key = {"print-metrics", "m"})
    public void metrics(
            @ShellOption(help = "printing interval (<=0 toggles)", defaultValue = "0") int interval) {
        if (interval <= 0) {
            if (statsFuture.isDone()) {
                this.statsFuture = scheduler.scheduleAtFixedRate(CallStats::printStdOut, 1, PRINT_INTERVAL);
                console.info("Metrics printing started with %ds interval", PRINT_INTERVAL);
            } else {
                this.statsFuture.cancel(true);
                console.info("Metrics printing stopped");
            }
        } else {
            if (!this.statsFuture.isDone()) {
                this.statsFuture.cancel(true);
                console.info("Metrics printing stopped");
            }
            PRINT_INTERVAL = interval;
            this.statsFuture = scheduler.scheduleAtFixedRate(CallStats::printStdOut, 1, PRINT_INTERVAL);
            console.info("Metrics printing started with %ds interval", PRINT_INTERVAL);
        }
    }

}
