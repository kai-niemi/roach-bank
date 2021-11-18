package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.ThrottledExecutor;
import io.roach.bank.client.support.TraversonHelper;

import static io.roach.bank.api.BankLinkRelations.ADMIN_REL;
import static io.roach.bank.api.BankLinkRelations.POOL_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Pool {
    @Autowired
    private ThrottledExecutor throttledExecutor;

    @Autowired
    private Console console;

    @Autowired
    private TraversonHelper traverson;

    @Autowired
    private RestTemplate restTemplate;

    @ShellMethod(value = "Print thread pool status", key = {"thread-pool-get", "tpg"})
    public void getPoolSize() {
        throttledExecutor.printStatus(executorService -> {
            console.info("Executor Stats:");
            console.debug("activeCount: %d", executorService.getActiveCount());
            console.debug("taskCount: %d", executorService.getTaskCount());
            console.debug("completedTaskCount: %d", executorService.getCompletedTaskCount());
            console.debug("corePoolSize: %d", executorService.getCorePoolSize());
            console.debug("poolSize: %d", executorService.getPoolSize());
            console.debug("largestPoolSize: %d", executorService.getLargestPoolSize());
            console.debug("maximumPoolSize: %d", executorService.getMaximumPoolSize());
            console.debug("isShutdown: %s", executorService.isShutdown());
            console.debug("isTerminated: %s", executorService.isTerminated());
            console.debug("isTerminating: %s", executorService.isTerminating());
            console.debug("queue: %d:", executorService.getQueue().size());
        });
    }

    @ShellMethod(value = "Set thread pool size", key = {"thread-pool-set", "tps"})
    public void setPoolSize(
            @ShellOption(help = "core thread pool size (guide: 2x vCPUs of host)") int size,
            @ShellOption(help = "thread queue size (guide: 2x pool size)", defaultValue = "-1") int queueSize) {
        if (queueSize < 0) {
            queueSize = size * 2;
        }
        if (queueSize < size) {
            throw new IllegalArgumentException("Queue size must be >= thread size");
        }
        console.info("Setting thread pool size to %d queue size %d\n", size, queueSize);
        throttledExecutor.cancelAndRestart(size, queueSize);
    }

    @ShellMethod(value = "Set connection pool size (server side)", key = {"conn-pool-set", "cps"})
    public void setConnPoolSize(
            @ShellOption(help = "max and min idle pool size", defaultValue = "50") int size) {
        console.info("Setting conn pool size to %d\n", size);

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
            console.warn("Unexpected HTTP status: %s", response.toString());
        }
    }
}
