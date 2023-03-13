package io.roach.bank.client.command;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.command.support.CallMetrics;
import jakarta.annotation.PostConstruct;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Metrics extends AbstractCommand {
    private boolean printMetrics = true;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    @Qualifier("workloadExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private CallMetrics callMetrics;

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(callMetricsPrinter(), 5, 5, TimeUnit.SECONDS);
    }

    @ShellMethod(value = "Reset call metrics", key = {"reset-metrics", "rm"})
    public void resetStats() {
        callMetrics.reset();
        console.successf("Metrics reset\n");
    }

    @ShellMethod(value = "Toggle console metrics", key = {"toggle-metrics", "m"})
    public void toggleMetrics() {
        printMetrics = !printMetrics;
        console.successf("Metrics printing is %s", printMetrics ? "on" : "off");
    }

    private Runnable callMetricsPrinter() {
        return () -> {
            if (printMetrics && threadPoolTaskExecutor.getActiveCount() > 0) {
                console.otherf(AnsiColor.BRIGHT_GREEN, "%s", callMetrics.prettyPrintHeader());
                console.otherf(AnsiColor.BRIGHT_YELLOW, "%s", callMetrics.prettyPrintBody());
                console.otherf(AnsiColor.BRIGHT_MAGENTA, "%s", callMetrics.prettyPrintFooter());
            }
        };
    }
}
