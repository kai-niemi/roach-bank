package io.roach.bank.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtils {
    private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);

    public static String millisecondsToDisplayString(long timeMillis) {
        double seconds = (timeMillis / 1000.0) % 60;
        int minutes = (int) ((timeMillis / 60000) % 60);
        int hours = (int) ((timeMillis / 3600000));

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format("%dh", hours));
        }
        if (hours > 0 || minutes > 0) {
            sb.append(String.format("%dm", minutes));
        }
        if (hours == 0 && seconds > 0) {
            sb.append(String.format(Locale.US, "%.1fs", seconds));
        }
        return sb.toString();
    }

    public static <V> long executionTime(Callable<V> task) {
        try {
            long start = System.nanoTime();
            task.call();
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
            logger.debug("{} completed in {}", task, millisecondsToDisplayString(millis));
            return millis;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
