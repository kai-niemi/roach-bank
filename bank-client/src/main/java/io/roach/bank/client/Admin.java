package io.roach.bank.client;

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
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.DurationFormat;
import io.roach.bank.client.support.HypermediaClient;

import static io.roach.bank.api.LinkRelations.ACTUATOR_REL;
import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class Admin implements Quit.Command {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private Console console;

    @Autowired
    private HypermediaClient hypermediaClient;

    @ShellMethod(value = "Exit the shell", key = {"quit", "exit", "q"})
    public void quit() {
        applicationContext.close();
        throw new ExitRequest();
    }

    @ShellMethod(value = "Shutdown server", key = {"shutdown"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void shutdown() {
        Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(ACTUATOR_REL))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri()).path("/shutdown");

        ResponseEntity<MessageModel> response = hypermediaClient
                .post(Link.of(builder.build().toUriString()), MessageModel.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.success("Unexpected HTTP status: %s", response.toString());
        } else {
            console.error("%s", response.getBody().getMessage());
        }
    }

    @ShellMethod(value = "Print application uptime")
    public void uptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        console.info("%s", DurationFormat.millisecondsToDisplayString(uptime));
    }

    @ShellMethod(value = "Print system information", key = {"system-info", "si"})
    public void systemInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        console.success(">> OS");
        console.info(" Arch: %s | OS: %s | Version: %s", os.getArch(), os.getName(), os.getVersion());
        console.info(" Available processors: %d", os.getAvailableProcessors());
        console.info(" Load avg: %f", os.getSystemLoadAverage());

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        console.success(">> Runtime");
        console.info(" Uptime: %s", r.getUptime());
        console.info(" VM name: %s | Vendor: %s | Version: %s", r.getVmName(), r.getVmVendor(), r.getVmVersion());

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        console.success(">> Runtime");
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
        console.success(">> Memory");
        console.info(" Heap: %s", m.getHeapMemoryUsage().toString());
        console.info(" Non-heap: %s", m.getNonHeapMemoryUsage().toString());
        console.info(" Pending GC: %s", m.getObjectPendingFinalizationCount());
    }
}
