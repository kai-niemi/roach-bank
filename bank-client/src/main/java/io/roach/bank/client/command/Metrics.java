package io.roach.bank.client.command;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.support.CallMetrics;
import io.roach.bank.client.support.Console;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Metrics {
    private boolean printMetrics = true;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private Console console;

    @Autowired
    private CallMetrics callMetrics;

    @ShellMethod(value = "Toggle console metrics", key = {"metrics", "m"})
    public void toggleMetrics() {
        printMetrics = !printMetrics;
        console.green("Metrics printing is %s\n", printMetrics ? "on" : "off");
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(callMetricsPrinter(), 5, 5, TimeUnit.SECONDS);
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
