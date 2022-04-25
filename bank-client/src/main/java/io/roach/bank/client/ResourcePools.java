package io.roach.bank.client;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.RestCommands;
import io.roach.bank.client.support.ThreadPoolStats;

import static io.roach.bank.api.LinkRelations.*;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class ResourcePools {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private Console console;

    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Set client thread pool and server connection pool sizes", key = {"set-pool-size", "sps"})
    public void setPoolSize(
            @ShellOption(help = "thread pool size", defaultValue = "50") int threadPoolSize,
            @ShellOption(help = "connection pool size", defaultValue = "50") int connPoolSize
    ) {
        console.yellow("Setting client thread pool size to %d\n", threadPoolSize);

        threadPoolTaskExecutor.setCorePoolSize(threadPoolSize);

        console.yellow("Setting server connection pool size to %d\n", connPoolSize);

        Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                .replaceQueryParam("size", connPoolSize);

        ResponseEntity<String> response = restCommands.post(Link.of(builder.build().toUriString()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.yellow("Unexpected HTTP status: %s\n", response.toString());
        }
    }

    @ShellMethod(value = "Print thread pool information", key = {"get-pool-size", "gps"})
    public void getPoolSize(@ShellOption(help = "repeat period in seconds", defaultValue = "0") int repeatTime) {
        Runnable r = () -> {
            ThreadPoolStats stats = ThreadPoolStats.from(threadPoolTaskExecutor);
            console.yellow("Thread pool status:\n");
            console.yellow("\tpoolSize: %s\n", stats.poolSize);
            console.yellow("\tmaximumPoolSize: %s\n", stats.maximumPoolSize);
            console.yellow("\tcorePoolSize: %s\n", stats.corePoolSize);
            console.yellow("\tactiveCount: %s\n", stats.activeCount);
            console.yellow("\tcompletedTaskCount: %s\n", stats.completedTaskCount);
            console.yellow("\ttaskCount: %s\n", stats.taskCount);
            console.yellow("\tlargestPoolSize: %s\n", stats.largestPoolSize);

            ResponseEntity<String> response = restCommands.fromRoot()
                    .follow(withCurie(ADMIN_REL))
                    .follow(withCurie(POOL_INFO_REL))
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                console.yellow("Connection pool info: %s\n", response.getBody());
            } else {
                console.red("Unexpected HTTP status: %s\n", response.toString());
            }
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
