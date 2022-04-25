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
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import io.roach.bank.client.support.RestCommands;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.POOL_INFO_REL;
import static io.roach.bank.api.LinkRelations.withCurie;
import static java.nio.charset.StandardCharsets.UTF_8;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Config extends AbstractCommand {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private RestCommands restCommands;

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

    @ShellMethod(value = "Print database information (server)", key = {"db-info", "di"})
    public void dbInfo(@ShellOption(help = "repeat period in seconds", defaultValue = "0") int repeatTime) {
        Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_INFO_REL))
                .asLink();

        ResponseEntity<String> response = restCommands.get(submitLink);
        if (!response.getStatusCode().is2xxSuccessful()) {
            console.yellow("Unexpected HTTP status: %s\n", response.toString());
        } else {
            console.yellow("Connection pool size: %s\n", response.getBody());
        }
    }

    @ShellMethod(value = "Print gateway region", key = {"gateway-region"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void gatewayRegion() {
        console.cyan("%s\n", restCommands.getGatewayRegion());
    }

    @ShellMethod(value = "List regions", key = {"regions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        console.yellow("-- regions --\n");
        restCommands.getRegions().forEach((k, v) -> {
            console.cyan("%s: %s\n", k, v);
        });
    }

    @ShellMethod(value = "List region cities", key = {"region-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "") String regions) {
        console.yellow("-- region cities --\n");
        restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.cyan("%s\n", s);
        });
    }

    @ShellMethod(value = "List cities", key = {"cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listCities() {
        console.yellow("-- cities --\n");
        restCommands.getCities().forEach(s -> {
            console.cyan("%s\n", s);
        });
    }

    @ShellMethod(value = "List city currency", key = {"city-currency"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listCityCurrency() {
        console.yellow("-- city / currency --\n");
        restCommands.getCityCurrency().forEach((s, currency) -> {
            console.cyan("%s: %s\n", s, currency);
        });
    }
}
