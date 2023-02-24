package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.command.support.ThreadPoolStats;

import static io.roach.bank.api.LinkRelations.*;

@ShellComponent
@ShellCommandGroup(Constants.POOL_COMMANDS)
public class ResourcePools extends AbstractCommand {
    @Autowired
    @Qualifier("workloadExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Get server connection pool size", key = {"get-pool-size", "gps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getPoolSize() {
        ResponseEntity<String> configResponse = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .toEntity(String.class);

        console.infof("Connection pool size:\n");
        console.successf("%s", configResponse.getBody());
    }

    @ShellMethod(value = "Set server connection pool size", key = {"set-pool-size", "sps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void setPoolSize(@ShellOption(help = "connection pool size", defaultValue = "100") int size
    ) {
        console.successf("Setting connection pool size to %d", size);

        Link submitLink = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                .replaceQueryParam("size", size);

        ResponseEntity<String> response = restCommands.post(Link.of(builder.build().toUriString()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.successf("Unexpected HTTP status: %s", response.toString());
        }
    }

    @ShellMethod(value = "Get server connection pool config", key = {"get-pool-config", "gpc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getPoolConfig() {
        ResponseEntity<String> response = restCommands.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_CONFIG_REL))
                .toEntity(String.class);

        console.infof("Connection pool config:\n");
        console.successf("%s", response.getBody());
    }

    @ShellMethod(value = "Set local thread pool size", key = {"set-thread-pool-size", "stps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void setThreadPoolSize(@ShellOption(help = "connection pool size", defaultValue = "-1") int size) {
        size = size > 0 ? size : Runtime.getRuntime().availableProcessors() * 8;
        threadPoolTaskExecutor.setCorePoolSize(size);
        console.infof("Thread pool size set to %d", size);
    }

    @ShellMethod(value = "Get local thread pool size", key = {"get-thread-pool-size", "gtps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getThreadPoolSize() {
        ThreadPoolStats stats = ThreadPoolStats.from(threadPoolTaskExecutor);
        console.infof("Thread pool stats:");
        console.successf("\tpoolSize: %s", stats.poolSize);
        console.successf("\tmaximumPoolSize: %s", stats.maximumPoolSize);
        console.successf("\tcorePoolSize: %s", stats.corePoolSize);
        console.successf("\tactiveCount: %s", stats.activeCount);
        console.successf("\tcompletedTaskCount: %s", stats.completedTaskCount);
        console.successf("\ttaskCount: %s", stats.taskCount);
        console.successf("\tlargestPoolSize: %s", stats.largestPoolSize);
    }
}
