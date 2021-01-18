package io.roach.bank.client.util;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TimeFormat {
    private static final Pattern DURATION_PATTERN = Pattern.compile("([0-9]+)([smhdw])");

    private TimeFormat() {
    }

    public static Duration parseDuration(String duration) {
        Matcher matcher = DURATION_PATTERN.matcher(duration.toLowerCase(Locale.ENGLISH));
        Duration instant = Duration.ZERO;
        while (matcher.find()) {
            int ordinal = Integer.parseInt(matcher.group(1));
            String token = matcher.group(2);
            switch (token) {
                case "s":
                    instant = instant.plus(Duration.ofSeconds(ordinal));
                    break;
                case "m":
                    instant = instant.plus(Duration.ofMinutes(ordinal));
                    break;
                case "h":
                    instant = instant.plus(Duration.ofHours(ordinal));
                    break;
                case "d":
                    instant = instant.plus(Duration.ofDays(ordinal));
                    break;
                case "w":
                    instant = instant.plus(Duration.ofDays(ordinal * 7));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid token " + token);
            }
        }
        if (instant.equals(Duration.ZERO)) {
            return Duration.ofSeconds(Integer.parseInt(duration));
        }
        return instant;
    }
}
