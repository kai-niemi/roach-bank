package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.HttpServerErrorException;

import io.roach.bank.api.support.RandomData;
import io.roach.bank.client.support.ThrottledExecutor;
import io.roach.bank.client.support.TimeDuration;
import io.roach.bank.client.util.DurationFormat;

@ShellComponent
@ShellCommandGroup(Constants.ADMIN_COMMANDS)
public class SpinLoop {
    @Autowired
    private ThrottledExecutor throttledExecutor;

    @ShellMethod("Spin loop that does nothing but eat cycles")
    public void spin(
            @ShellOption(help = "delay range in millis between spins", defaultValue = "15-75") String delayMillis,
            @ShellOption(help = "error ratio (0-1.0)", defaultValue = "0.01") double errorRatio,
            @ShellOption(help = "group count", defaultValue = "1") int groups,
            @ShellOption(help = Constants.DURATION_HELP, defaultValue = Constants.DEFAULT_DURATION) String duration,
            @ShellOption(help = Constants.CONC_HELP, defaultValue = "-1") int concurrency
    ) {
        if (concurrency <= 0) {
            concurrency = Runtime.getRuntime().availableProcessors() * 2;
        }

        final String p[] = delayMillis.split("-");
        final int low;
        final int high;

        if (p.length > 1) {
            low = Integer.parseInt(p[0]);
            high = Integer.parseInt(p[1]);
        } else {
            low = high = Integer.parseInt(p[0]);
        }

        List<String> groupList = new ArrayList<>();

        IntStream.range(0, groups).forEach(value -> {
            groupList.add(RandomData.randomString(8));
        });

        for (String g : groupList) {
            IntStream.range(0, concurrency).forEach(i -> throttledExecutor.submit(() -> {
                Thread.sleep((long) (low + Math.random() * (high - low)));
                if (Math.random() < errorRatio) {
                    throw new HttpServerErrorException(HttpStatus.CONFLICT, "Fake disturbance!");
                }
                return null;
            }, TimeDuration.of(DurationFormat.parseDuration(duration)), g));
        }
    }
}
