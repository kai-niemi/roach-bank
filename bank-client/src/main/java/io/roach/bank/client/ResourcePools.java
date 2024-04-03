package io.roach.bank.client;

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

import io.roach.bank.client.support.HypermediaClient;
import io.roach.bank.client.support.ThreadPoolStats;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.POOL_CONFIG_REL;
import static io.roach.bank.api.LinkRelations.POOL_SIZE_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.POOL_COMMANDS)
public class ResourcePools extends AbstractCommand {
    @Autowired
    @Qualifier("workloadExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private HypermediaClient bankClient;

    @ShellMethod(value = "Get server connection pool size", key = {"get-pool-size", "gps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getPoolSize() {
        ResponseEntity<String> configResponse = bankClient.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .toEntity(String.class);

        console.info("Connection pool size:");
        console.success("%s", configResponse.getBody());
    }

    @ShellMethod(value = "Set server connection pool size", key = {"set-pool-size", "sps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void setPoolSize(@ShellOption(help = "connection pool size", defaultValue = "100") int size
    ) {
        console.success("Setting connection pool size to %d", size);

        Link submitLink = bankClient.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_SIZE_REL))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri())
                .replaceQueryParam("size", size);

        ResponseEntity<String> response = bankClient.post(Link.of(builder.build().toUriString()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.success("Unexpected HTTP status: %s", response.toString());
        }
    }

    @ShellMethod(value = "Get server connection pool config", key = {"get-pool-config", "gpc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getPoolConfig() {
        ResponseEntity<String> response = bankClient.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(POOL_CONFIG_REL))
                .toEntity(String.class);

        console.info("Connection pool config:");
        console.success("%s", response.getBody());
    }

    @ShellMethod(value = "Set local thread pool size", key = {"set-thread-pool-size", "stps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void setThreadPoolSize(@ShellOption(help = "connection pool size", defaultValue = "500") int size) {
        threadPoolTaskExecutor.setMaxPoolSize(size);
        threadPoolTaskExecutor.setCorePoolSize(size);
        console.info("Thread pool size set to %d", size);
    }

    @ShellMethod(value = "Get local thread pool size", key = {"get-thread-pool-size", "gtps"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void getThreadPoolSize() {
        ThreadPoolStats stats = ThreadPoolStats.from(threadPoolTaskExecutor);
        console.info("Thread pool stats:");
        console.success("\tpoolSize: %s", stats.poolSize);
        console.success("\tmaximumPoolSize: %s", stats.maximumPoolSize);
        console.success("\tcorePoolSize: %s", stats.corePoolSize);
        console.success("\tactiveCount: %s", stats.activeCount);
        console.success("\tcompletedTaskCount: %s", stats.completedTaskCount);
        console.success("\ttaskCount: %s", stats.taskCount);
        console.success("\tlargestPoolSize: %s", stats.largestPoolSize);
    }
}
