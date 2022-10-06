package io.roach.bank.client;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.support.CallMetrics;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Metrics extends AbstractCommand {
    private boolean printMetrics = true;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    @Qualifier("jobExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private CallMetrics callMetrics;

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(callMetricsPrinter(), 5, 5, TimeUnit.SECONDS);
    }

    @ShellMethod(value = "Clear metrics", key = {"clear-metrics", "cm"})
    public void clearStats() {
        callMetrics.clear();
        console.green("Metrics cleared\n");
    }

    @ShellMethod(value = "Toggle console metrics", key = {"toggle-metrics", "tm"})
    public void toggleMetrics() {
        printMetrics = !printMetrics;
        console.green("Metrics printing is %s\n", printMetrics ? "on" : "off");
    }

    private Runnable callMetricsPrinter() {
        return () -> {
            if (printMetrics && threadPoolTaskExecutor.getActiveCount() > 0) {
                console.cyan("%s", callMetrics.prettyPrintHeader());
                console.white("%s", callMetrics.prettyPrintBody());
                console.green("%s", callMetrics.prettyPrintFooter());
            }
        };
    }
}
