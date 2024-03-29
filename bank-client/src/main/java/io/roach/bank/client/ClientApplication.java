package io.roach.bank.client;

import io.roach.bank.client.command.event.ConnectionUpdatedEvent;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.shell.jline.PromptProvider;

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackages = "io.roach.bank.client")
public class ClientApplication implements PromptProvider {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .logStartupInfo(true)
                .run(args);
    }

    private String connection;

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.connection = event.getBaseUri().getHost();
    }

    @Override
    public AttributedString getPrompt() {
        if (connection != null) {
            return new AttributedString(connection + ":$ ", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        } else {
            return new AttributedString("disconnected:$ ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        }
    }
}
