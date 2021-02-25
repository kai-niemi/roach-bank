package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.TimeDuration;
import io.roach.bank.client.util.DurationFormat;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class Head extends RestCommandSupport {
    @ShellMethod(value = "Send HEAD request to API root", key = {"head", "h"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void head(
            @ShellOption(help = "group count", defaultValue = "8") int groups,
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency,
            @ShellOption(help = "execution duration (use NwNdNhNmNs)", defaultValue = Constants.DEFAULT_DURATION)
                    String duration) {
        if (concurrency <= 0) {
            concurrency = Runtime.getRuntime().availableProcessors() * 2;
            concurrency = Math.max(1, concurrency / Math.max(1, groups));
        }

        List<String> groupList = new ArrayList<>();

        IntStream.range(0, groups).forEach(value -> groupList.add(RandomData.randomString(8)));

        for (String group : groupList) {
            IntStream.range(0, concurrency).forEach(i -> throttledExecutor.submit(() -> {
                        restTemplate.headForHeaders(baseUri);
                        return null;
                    }, TimeDuration.of(DurationFormat.parseDuration(duration)),
                    group));
        }
    }
}
