package io.roach.bank.client.support;

import java.util.Locale;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class Console {
    private final Terminal terminal;

    @Autowired
    public Console(@Lazy Terminal terminal) {
        Assert.notNull(terminal, "terminal is null");
        this.terminal = terminal;
    }

    public void debug(String format, Object... args) {
        custom(Color.GREEN, format, args);
    }

    public void info(String format, Object... args) {
        custom(Color.CYAN, format, args);
    }

    public void warn(String format, Object... args) {
        custom(Color.YELLOW, format, args);
    }

    public void error(String format, Object... args) {
        custom(Color.RED, format, args);
    }

    public void custom(Color color, String format, Object... args) {
        terminal.writer().println(color(color, String.format(Locale.US, format, args)));
        terminal.writer().flush();
    }

    private String color(Color color, String message) {
        return new AttributedStringBuilder()
                .append(message, AttributedStyle.DEFAULT.foreground(color.code()).backgroundOff())
                .toAnsi();
    }

    public enum Color {
        BLACK(0),
        RED(1),
        GREEN(2),
        YELLOW(3),
        BLUE(4),
        MAGENTA(5),
        CYAN(6),
        WHITE(7),
        BRIGHT(8);

        private final int code;

        Color(int code) {
            this.code = code;
        }

        public int code() {
            return this.code;
        }
    }
}
