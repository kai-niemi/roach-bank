package io.roach.bank.client.command.support;

import java.util.Locale;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
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

    public void success(String text) {
        text(AnsiColor.BRIGHT_GREEN, text);
    }

    public void successf(String format, Object... args) {
        textf(AnsiColor.BRIGHT_GREEN, format, args);
    }

    public void info(String text) {
        text(AnsiColor.BRIGHT_BLUE, text);
    }

    public void infof(String format, Object... args) {
        textf(AnsiColor.BRIGHT_CYAN, format, args);
    }

    public void warn(String text) {
        text(AnsiColor.BRIGHT_YELLOW, text);
    }

    public void warnf(String format, Object... args) {
        textf(AnsiColor.BRIGHT_YELLOW, format, args);
    }

    public void error(String text) {
        text(AnsiColor.BRIGHT_RED, text);
    }

    public void errorf(String format, Object... args) {
        textf(AnsiColor.BRIGHT_RED, format, args);
    }

    public void text(AnsiColor color, String text) {
        terminal.writer().println(ansiColor(color, text));
        terminal.writer().flush();
    }

    public void textf(AnsiColor color, String format, Object... args) {
        terminal.writer().println(ansiColor(color, String.format(Locale.US, format, args)));
        terminal.writer().flush();
    }

    public void otherf(AnsiColor color, String format, Object... args) {
        terminal.writer().printf(ansiColor(color, String.format(Locale.US, format, args)));
        terminal.writer().flush();
    }

    private String ansiColor(AnsiColor color, String message) {
        return AnsiOutput.toString(color, message, AnsiColor.DEFAULT);
    }
}
