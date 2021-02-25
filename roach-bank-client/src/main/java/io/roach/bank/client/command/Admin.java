package io.roach.bank.client.command;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.Quit;
import org.springframework.util.FileCopyUtils;

import io.roach.bank.client.support.CallStats;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.SchedulingHelper;
import io.roach.bank.client.support.ThrottledExecutor;

import static java.nio.charset.StandardCharsets.UTF_8;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Admin implements Quit.Command {
    public static final int PERIOD_SECONDS = 5;

    private static int PRINT_INTERVAL = PERIOD_SECONDS;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ThrottledExecutor throttledExecutor;

    @Autowired
    private Console console;

    @Autowired
    private SchedulingHelper scheduler;

    private ScheduledFuture<?> statsFuture;

    @PostConstruct
    protected void init() {
        this.statsFuture = scheduler.scheduleAtFixedRate(CallStats::printStdOut, PRINT_INTERVAL, PRINT_INTERVAL);
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ShellMethod(value = "Exit the shell", key = {"quit", "exit", "q"})
    public void quit() {
//        logger.debug("debug");
//        logger.info("info");
//        logger.warn("warn");
//        logger.error("error");
        applicationContext.close();
        throw new ExitRequest();
    }

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

    @ShellMethod(value = "Print application YAML config")
    public void printConfig() {
        Resource resource = applicationContext.getResource("classpath:application.yml");
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            System.out.println(FileCopyUtils.copyToString(reader));
            System.out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @ShellMethod(value = "Print worker pool status", key = {"print-stats", "ps"})
    public void printStats() {
        throttledExecutor.printStatus();
    }

    @ShellMethod(value = "Frequency for printing per-operation metrics (default is 5s)", key = {"metrics", "m"})
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

    @ShellMethod(value = "Print local system information")
    public void printInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        console.custom(Console.Color.YELLOW, ">> OS");
        console.info(" Arch: %s | OS: %s | Version: %s", os.getArch(), os.getName(), os.getVersion());
        console.info(" Available processors: %d", os.getAvailableProcessors());
        console.info(" Load avg: %f", os.getSystemLoadAverage());

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        console.custom(Console.Color.YELLOW, ">> Runtime");
        console.info(" Uptime: %s", r.getUptime());
        console.info(" VM name: %s | Vendor: %s | Version: %s", r.getVmName(), r.getVmVendor(), r.getVmVersion());

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        console.custom(Console.Color.YELLOW, ">> Runtime");
        console.info(" Peak threads: %d", t.getPeakThreadCount());
        console.info(" Thread #: %d", t.getThreadCount());
        console.info(" Total started threads: %d", t.getTotalStartedThreadCount());

        Arrays.stream(t.getAllThreadIds()).sequential().forEach(value -> {
            console.info(" Thread (%d): %s %s", value,
                    t.getThreadInfo(value).getThreadName(),
                    t.getThreadInfo(value).getThreadState().toString()
            );
        });

        MemoryMXBean m = ManagementFactory.getMemoryMXBean();
        console.custom(Console.Color.YELLOW, ">> Memory");
        console.info(" Heap: %s", m.getHeapMemoryUsage().toString());
        console.info(" Non-heap: %s", m.getNonHeapMemoryUsage().toString());
        console.info(" Pending GC: %s", m.getObjectPendingFinalizationCount());
    }
}
