package io.roach.bank.client.command;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.commands.Quit;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.api.MessageModel;
import io.roach.bank.client.command.support.Console;
import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.util.DurationFormat;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.TOGGLE_TRACE_LOG;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Admin implements Quit.Command {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private RestCommands restCommands;

    @Autowired
    private Console console;

    @ShellMethod(value = "Exit the shell", key = {"quit", "exit", "q"})
    public void quit() {
        applicationContext.close();
        throw new ExitRequest();
    }

    @ShellMethod(value = "Print application uptime")
    public void uptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        console.infof("%s", DurationFormat.millisecondsToDisplayString(uptime));
    }

    @ShellMethod(value = "Print system information", key = {"system-info", "si"})
    public void systemInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        console.successf(">> OS");
        console.infof(" Arch: %s | OS: %s | Version: %s", os.getArch(), os.getName(), os.getVersion());
        console.infof(" Available processors: %d", os.getAvailableProcessors());
        console.infof(" Load avg: %f", os.getSystemLoadAverage());

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        console.successf(">> Runtime");
        console.infof(" Uptime: %s", r.getUptime());
        console.infof(" VM name: %s | Vendor: %s | Version: %s", r.getVmName(), r.getVmVendor(), r.getVmVersion());

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        console.successf(">> Runtime");
        console.infof(" Peak threads: %d", t.getPeakThreadCount());
        console.infof(" Thread #: %d", t.getThreadCount());
        console.infof(" Total started threads: %d", t.getTotalStartedThreadCount());

        Arrays.stream(t.getAllThreadIds()).sequential().forEach(value -> {
            console.infof(" Thread (%d): %s %s", value,
                    t.getThreadInfo(value).getThreadName(),
                    t.getThreadInfo(value).getThreadState().toString()
            );
        });

        MemoryMXBean m = ManagementFactory.getMemoryMXBean();
        console.successf(">> Memory");
        console.infof(" Heap: %s", m.getHeapMemoryUsage().toString());
        console.infof(" Non-heap: %s", m.getNonHeapMemoryUsage().toString());
        console.infof(" Pending GC: %s", m.getObjectPendingFinalizationCount());
    }

    @ShellMethod(value = "Toggle SQL trace logging (server side)", key = {"trace"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void toggleSqlTraceLogging() {
        Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(TOGGLE_TRACE_LOG))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri());

        ResponseEntity<MessageModel> response = restCommands
                .post(Link.of(builder.build().toUriString()), MessageModel.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.successf("Unexpected HTTP status: %s", response.toString());
        } else {
            console.errorf("%s", response.getBody().getMessage());
        }
    }
}
