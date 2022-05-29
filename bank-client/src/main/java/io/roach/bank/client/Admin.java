package io.roach.bank.client;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.commands.Quit;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.api.MessageModel;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.TOGGLE_TRACE_LOG;
import static io.roach.bank.api.LinkRelations.withCurie;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    @ShellMethod(value = "Print system information", key = {"system-info", "si"})
    public void systemInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        console.yellow(">> OS\n");
        console.cyan(" Arch: %s | OS: %s | Version: %s\n", os.getArch(), os.getName(), os.getVersion());
        console.cyan(" Available processors: %d\n", os.getAvailableProcessors());
        console.cyan(" Load avg: %f\n", os.getSystemLoadAverage());

        RuntimeMXBean r = ManagementFactory.getRuntimeMXBean();
        console.yellow(">> Runtime\n");
        console.cyan(" Uptime: %s\n", r.getUptime());
        console.cyan(" VM name: %s | Vendor: %s | Version: %s\n", r.getVmName(), r.getVmVendor(), r.getVmVersion());

        ThreadMXBean t = ManagementFactory.getThreadMXBean();
        console.yellow(">> Runtime\n");
        console.cyan(" Peak threads: %d\n", t.getPeakThreadCount());
        console.cyan(" Thread #: %d\n", t.getThreadCount());
        console.cyan(" Total started threads: %d\n", t.getTotalStartedThreadCount());

        Arrays.stream(t.getAllThreadIds()).sequential().forEach(value -> {
            console.cyan(" Thread (%d): %s %s\n", value,
                    t.getThreadInfo(value).getThreadName(),
                    t.getThreadInfo(value).getThreadState().toString()
            );
        });

        MemoryMXBean m = ManagementFactory.getMemoryMXBean();
        console.yellow(">> Memory\n");
        console.cyan(" Heap: %s\n", m.getHeapMemoryUsage().toString());
        console.cyan(" Non-heap: %s\n", m.getNonHeapMemoryUsage().toString());
        console.cyan(" Pending GC: %s\n", m.getObjectPendingFinalizationCount());
    }

    @ShellMethod(value = "Toggle SQL trace logging (server side)", key = {"toggle-trace", "l"})
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
            console.yellow("Unexpected HTTP status: %s\n", response.toString());
        } else {
            console.green("%s\n", response.getBody().getMessage());
        }
    }
}
