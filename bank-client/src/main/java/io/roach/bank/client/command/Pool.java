package io.roach.bank.client.command;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.support.BoundedExecutor;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.ThreadPoolStats;
import io.roach.bank.client.support.TraversonHelper;

import static io.roach.bank.api.BankLinkRelations.ADMIN_REL;
import static io.roach.bank.api.BankLinkRelations.POOL_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Pool {
    @Autowired
    private BoundedExecutor boundedExecutor;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private Console console;

    @Autowired
    private TraversonHelper traverson;

    @Autowired
    private RestTemplate restTemplate;

    @ShellMethod(value = "Set connection pool size (server side)", key = {"conn-pool-set", "cps"})
    public void setConnPoolSize(
            @ShellOption(help = "max and min idle pool size", defaultValue = "50") int size) {
        console.yellow("Setting conn pool size to %d\n", size);

        Link submitLink = traverson.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_REL))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                .replaceQueryParam("size", size);

        String uri = builder.build().toUriString();

        ResponseEntity<String> response =
                restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(null),
                        String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.yellow("Unexpected HTTP status: %s\n", response.toString());
        }
    }

    @ShellMethod(value = "Configure thread pool size", key = {"thread-pool-set", "tps"})
    @ShellMethodAvailability("noActiveWorkersCheck")
    public void threadPoolSet(
            @ShellOption(help = "core thread pool size (guide: 2x vCPUs of host)") int size) {
        console.yellow("Setting thread pool size to %d\n", size);
        boundedExecutor.cancelAndRestart(size);
    }

    @ShellMethod(value = "Print thread pool information", key = {"thread-pool-get", "tpg"})
    public void threadPoolGet(@ShellOption(help = "repeat period in seconds", defaultValue = "0") int repeatTime) {
        Runnable r = () -> {
            ThreadPoolStats stats = ThreadPoolStats.from(boundedExecutor);
            console.yellow("Thread pool status:\n");
            console.yellow("\tpoolSize: %s\n", stats.poolSize);
            console.yellow("\tmaximumPoolSize: %s\n", stats.maximumPoolSize);
            console.yellow("\tcorePoolSize: %s\n", stats.corePoolSize);
            console.yellow("\tactiveCount: %s\n", stats.activeCount);
            console.yellow("\tcompletedTaskCount: %s\n", stats.completedTaskCount);
            console.yellow("\ttaskCount: %s\n", stats.taskCount);
            console.yellow("\tlargestPoolSize: %s\n", stats.largestPoolSize);
        };

        if (repeatTime > 0) {
            ScheduledFuture<?> f = scheduledExecutorService
                    .scheduleAtFixedRate(r, 0, 2, TimeUnit.SECONDS);
            scheduledExecutorService
                    .schedule(() -> {
                        f.cancel(true);
                    }, repeatTime, TimeUnit.SECONDS);
        } else {
            r.run();
        }
    }

}
