package io.roach.bank.client;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.web.util.UriComponentsBuilder;

import io.roach.bank.api.MessageModel;
import io.roach.bank.client.support.CallMetrics;
import io.roach.bank.client.support.HypermediaClient;
import jakarta.annotation.PostConstruct;

import static io.roach.bank.api.LinkRelations.ADMIN_REL;
import static io.roach.bank.api.LinkRelations.TOGGLE_TRACE_LOG;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.LOGGING_COMMANDS)
public class Logging extends AbstractCommand {
    private boolean printMetrics = true;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    @Qualifier("workloadExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private CallMetrics callMetrics;

    @Autowired
    private HypermediaClient hypermediaClient;

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(callMetricsPrinter(), 5, 5, TimeUnit.SECONDS);
    }

    private Runnable callMetricsPrinter() {
        return () -> {
            if (printMetrics && threadPoolTaskExecutor.getActiveCount() > 0) {
                callMetrics.prettyPrint(console);
            }
        };
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

    @ShellMethod(value = "Toggle SQL trace logging (server side)", key = {"sql-trace", "st"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void toggleSqlTraceLogging() {
        Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(ADMIN_REL))
                .follow(withCurie(TOGGLE_TRACE_LOG))
                .asLink();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(submitLink.toUri());

        ResponseEntity<MessageModel> response = hypermediaClient
                .post(Link.of(builder.build().toUriString()), MessageModel.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            console.success("Unexpected HTTP status: %s", response.toString());
        } else {
            console.error("%s", response.getBody().getMessage());
        }
    }
}
