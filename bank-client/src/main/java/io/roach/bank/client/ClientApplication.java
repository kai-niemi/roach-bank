package io.roach.bank.client;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.shell.jline.PromptProvider;

import io.roach.bank.client.event.ClearErrorsEvent;
import io.roach.bank.client.event.ConnectionUpdatedEvent;
import io.roach.bank.client.event.ExecutionErrorEvent;
import io.roach.bank.client.support.Console;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackages = "io.roach.bank.client")
public class ClientApplication implements PromptProvider {
    @Autowired
    private Console console;

    private transient String connection;

    private transient boolean errors;

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .logStartupInfo(true)
                .run(args);
    }

    @Override
    public AttributedString getPrompt() {
        if (connection != null) {
            return new AttributedString(connection
                    + (errors ? " (ERROR)" : "") + ":$ ",
                    AttributedStyle.DEFAULT.foreground(errors ? AttributedStyle.RED : AttributedStyle.GREEN));
        } else {
            return new AttributedString("disconnected"
                    + (errors ? " (ERROR)" : "") + ":$ ",
                    AttributedStyle.DEFAULT.foreground(errors ? AttributedStyle.RED : AttributedStyle.YELLOW));
        }
    }

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.connection = event.getBaseUri().getHost();
    }

    @EventListener
    public void handle(ExecutionErrorEvent event) {
        errors = true;
    }

    @EventListener
    public void handle(ClearErrorsEvent event) {
        console.success("Errors cleared");
        errors = false;
    }
}
