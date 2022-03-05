package io.roach.bank.client.command;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

import io.roach.bank.client.support.Console;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Admin implements Quit.Command {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private Console console;

    @ShellMethod(value = "Exit the shell", key = {"quit", "exit", "q"})
    public void quit() {
        applicationContext.close();
        throw new ExitRequest();
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
