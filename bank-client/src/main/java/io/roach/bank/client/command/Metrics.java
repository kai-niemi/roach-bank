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
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import io.roach.bank.client.support.BoundedExecutor;
import io.roach.bank.client.support.CallMetric;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.ThreadPoolStats;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Metrics {
    private static final ConcurrentLinkedDeque<ThreadPoolStats> aggregatedThreadPoolStats
            = new ConcurrentLinkedDeque<>();

    private static final ConcurrentLinkedDeque<Double> aggregatedLoadAvg
            = new ConcurrentLinkedDeque<>();

    private boolean printMetrics = true;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private Console console;

    @Autowired
    private BoundedExecutor boundedExecutor;

    @ShellMethod(value = "Toggle console metrics", key = {"metrics", "m"})
    public void toggleMetrics() {
        printMetrics = !printMetrics;
        console.green("Metrics printing is %s\n", printMetrics ? "on" : "off");
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(poolMetricsSampler(), 5, 1, TimeUnit.SECONDS);
//        scheduledExecutorService.scheduleAtFixedRate(poolMetricsPrinter(), 10, 30, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(callMetricsPrinter(), 5, 5, TimeUnit.SECONDS);
    }

    private Runnable callMetricsPrinter() {
        return () -> {
            if (printMetrics && boundedExecutor.hasActiveWorkers()) {
                CallMetric callMetric = boundedExecutor.getCallMetric();
                console.cyan("%s", callMetric.prettyPrintHeader());
                console.white("%s", callMetric.prettyPrintBody());
                console.green("%s", callMetric.prettyPrintFooter());
            }
        };
    }

    private Runnable poolMetricsSampler() {
        return () -> {
            aggregatedThreadPoolStats.add(ThreadPoolStats.from(boundedExecutor));

            OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
            if (mxBean.getSystemLoadAverage() != -1) {
                aggregatedLoadAvg.add(mxBean.getSystemLoadAverage());
            }
        };
    }

    private Runnable poolMetricsPrinter() {
        return () -> {
            if (!printMetrics || !boundedExecutor.hasActiveWorkers()) {
                return;
            }

            try {
                ThreadPoolStats threadPoolStats = aggregatedThreadPoolStats.peekLast();
                if (threadPoolStats != null) {
                    printSummaryStats("poolSize",
                            threadPoolStats.poolSize,
                            aggregatedThreadPoolStats.stream().mapToInt(value -> value.poolSize));
                    printSummaryStats("largestPoolSize",
                            threadPoolStats.largestPoolSize,
                            aggregatedThreadPoolStats.stream().mapToInt(value -> value.largestPoolSize));
                    printSummaryStats("activeCount",
                            threadPoolStats.activeCount,
                            aggregatedThreadPoolStats.stream().mapToInt(value -> value.activeCount));
                    printSummaryStats("taskCount",
                            threadPoolStats.taskCount,
                            aggregatedThreadPoolStats.stream().mapToLong(value -> value.taskCount));
                    printSummaryStats("completedTaskCount",
                            threadPoolStats.completedTaskCount,
                            aggregatedThreadPoolStats.stream().mapToLong(value -> value.completedTaskCount));
                }

                if (!aggregatedLoadAvg.isEmpty()) {
                    printSummaryStats("loadavg",
                            aggregatedLoadAvg.getLast(),
                            aggregatedLoadAvg.stream().mapToDouble(value -> value));
                }
            } catch (Throwable e) {
                console.red("%s\n", e.toString());
            }
        };
    }

    private void printSummaryStats(String label, int current, IntStream histogram) {
        IntSummaryStatistics ss = histogram.summaryStatistics();
        if (ss.getCount() > 0) {
            console.yellow("%20s:", label);
            console.green(" current %d, min %d, max %d, avg %.0f, samples %d\n",
                    current,
                    ss.getMin(),
                    ss.getMax(),
                    ss.getAverage(),
                    ss.getCount());
        }
    }

    private void printSummaryStats(String label, long current, LongStream histogram) {
        LongSummaryStatistics ss = histogram.summaryStatistics();
        if (ss.getCount() > 0) {
            console.yellow("%20s:", label);
            console.green(" current %d, min %d, max %d, avg %.0f, samples %d\n",
                    current,
                    ss.getMin(),
                    ss.getMax(),
                    ss.getAverage(),
                    ss.getCount());
        }
    }

    private void printSummaryStats(String label, double current, DoubleStream histogram) {
        DoubleSummaryStatistics ss = histogram.summaryStatistics();
        if (ss.getCount() > 0) {
            console.yellow("%20s:", label);
            console.green(" current %.1f, min %.1f, max %.1f, avg %.1f, samples %d\n",
                    current,
                    ss.getMin(),
                    ss.getMax(),
                    ss.getAverage(),
                    ss.getCount());
        }
    }

    @ShellMethod(value = "Print system information", key = {"system-info", "si"})
    public void systemInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        console.yellow(">> OS\n");
        console.green(" Arch: %s | OS: %s | Version: %s\n", os.getArch(), os.getName(), os.getVersion());
        console.green(" Available processors: %d\n", os.getAvailableProcessors());
        console.green(" Load avg: %f\n", os.getSystemLoadAverage());

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        console.yellow(">> Runtime\n");
        console.green(" Uptime: %s\n", r.getUptime());
        console.green(" VM name: %s | Vendor: %s | Version: %s\n", r.getVmName(), r.getVmVendor(), r.getVmVersion());

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        console.yellow(">> Runtime\n");
        console.green(" Peak threads: %d\n", t.getPeakThreadCount());
        console.green(" Thread #: %d\n", t.getThreadCount());
        console.green(" Total started threads: %d\n", t.getTotalStartedThreadCount());

        Arrays.stream(t.getAllThreadIds()).sequential().forEach(value -> {
            console.green(" Thread (%d): %s %s\n", value,
                    t.getThreadInfo(value).getThreadName(),
                    t.getThreadInfo(value).getThreadState().toString()
            );
        });

        MemoryMXBean m = ManagementFactory.getMemoryMXBean();
        console.yellow(">> Memory\n");
        console.green(" Heap: %s\n", m.getHeapMemoryUsage().toString());
        console.green(" Non-heap: %s\n", m.getNonHeapMemoryUsage().toString());
        console.green(" Pending GC: %s\n", m.getObjectPendingFinalizationCount());
    }
}
