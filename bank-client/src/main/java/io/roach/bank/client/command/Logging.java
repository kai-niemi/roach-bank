package io.roach.bank.client.command;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.api.MessageModel;
import io.roach.bank.client.command.event.ExecutionErrorEvent;
import io.roach.bank.client.command.support.CallMetrics;
import io.roach.bank.client.command.support.HypermediaClient;
import jakarta.annotation.PostConstruct;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.TOGGLE_TRACE_LOG;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.LOGGING_COMMANDS)
public class Logging extends AbstractCommand {
    private boolean printMetrics = true;

    private final List<String> errors = new LinkedList<>();

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    @Qualifier("workloadExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private CallMetrics callMetrics;

    @Autowired
    private HypermediaClient bankClient;

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(callMetricsPrinter(), 5, 5, TimeUnit.SECONDS);
    }

    @ShellMethod(value = "Reset call metrics", key = {"clear-metrics", "cm"})
    public void clearMetrics() {
        callMetrics.clear();
        console.success("Call metrics cleared");
    }

    @ShellMethod(value = "Toggle console metrics", key = {"toggle-metrics", "m"})
    public void toggleMetrics() {
        printMetrics = !printMetrics;
        console.success("Metrics printing is %s", printMetrics ? "on" : "off");
    }

    @ShellMethod(value = "Print execution errors", key = {"print-errors", "pe"})
    public void printErrors(@ShellOption(defaultValue = "10") int limit) {
        errors.stream()
                .limit(limit)
                .forEach(s -> {
                    console.success(s);
                });
        if (errors.isEmpty()) {
            console.success("No errors");
        }
    }

    @ShellMethod(value = "Clear execution errors", key = {"clear-errors", "ce"})
    public void clearErrors() {
        errors.clear();
        console.success("Errors cleared");
    }

    @EventListener
    public void handle(ExecutionErrorEvent event) {
        if (errors.size() > 1000) {
            errors.remove(0);
        }
        errors.add(event.getMessage());
    }

    @ShellMethod(value = "Toggle SQL trace logging (server side)", key = {"trace"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void toggleSqlTraceLogging() {
        Link submitLink = bankClient.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(TOGGLE_TRACE_LOG))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri());

        ResponseEntity<MessageModel> response = bankClient
                .post(Link.of(builder.build().toUriString()), MessageModel.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.success("Unexpected HTTP status: %s", response.toString());
        } else {
            console.error("%s", response.getBody().getMessage());
        }
    }

    private Runnable callMetricsPrinter() {
        return () -> {
            if (printMetrics && threadPoolTaskExecutor.getActiveCount() > 0) {
                callMetrics.prettyPrint(console);
            }
        };
    }
}
