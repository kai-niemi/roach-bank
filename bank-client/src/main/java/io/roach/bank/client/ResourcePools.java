package io.roach.bank.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.support.RestCommands;
import io.roach.bank.client.support.ThreadPoolStats;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.POOL_CONFIG_REL;
import static io.roach.bank.api.LinkRelations.POOL_SIZE_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.POOL_COMMANDS)
public class ResourcePools extends AbstractCommand {
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Set server connection pool size", key = {"set-pool-size"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void setPoolSize(@ShellOption(help = "connection pool size", defaultValue = "100") int size
    ) {
        console.yellow("Setting connection pool size to %d\n", size);

        Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                .replaceQueryParam("size", size);

        ResponseEntity<String> response = restCommands.post(Link.of(builder.build().toUriString()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.yellow("Unexpected HTTP status: %s\n", response.toString());
        }
    }

    @ShellMethod(value = "Get server connection pool size", key = {"get-pool-size"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getPoolSize() {
        ResponseEntity<String> configResponse = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .toEntity(String.class);

        console.cyan("Connection pool size:");
        console.yellow("%s\n", configResponse.getBody());
    }

    @ShellMethod(value = "Get server connection pool config", key = {"get-pool-config"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getPoolConfig() {
        ResponseEntity<String> response = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_CONFIG_REL))
                .toEntity(String.class);

        console.cyan("Connection pool config:");
        console.yellow("%s\n", response.getBody());
    }

    @ShellMethod(value = "Get local thread pool size", key = {"get-thread-pool-size"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getThreadPoolSize() {
        ThreadPoolStats stats = ThreadPoolStats.from(threadPoolTaskExecutor);
        console.cyan("Thread pool stats:\n");
        console.yellow("\tpoolSize: %s\n", stats.poolSize);
        console.yellow("\tmaximumPoolSize: %s\n", stats.maximumPoolSize);
        console.yellow("\tcorePoolSize: %s\n", stats.corePoolSize);
        console.yellow("\tactiveCount: %s\n", stats.activeCount);
        console.yellow("\tcompletedTaskCount: %s\n", stats.completedTaskCount);
        console.yellow("\ttaskCount: %s\n", stats.taskCount);
        console.yellow("\tlargestPoolSize: %s\n", stats.largestPoolSize);
    }

    @ShellMethod(value = "Set local thread pool size", key = {"set-thread-pool-size"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void setThreadPoolSize(@ShellOption(help = "connection pool size", defaultValue = "-1") int size) {
        size = size > 0 ? size : Runtime.getRuntime().availableProcessors() * 4;
        threadPoolTaskExecutor.setCorePoolSize(size);
        console.cyan("Thread pool size set to %d", size);
    }
}
