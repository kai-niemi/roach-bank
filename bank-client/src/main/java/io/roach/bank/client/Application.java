package io.roach.bank.client;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.util.StringUtils;

import io.roach.bank.client.support.ConnectionUpdatedEvent;

@SpringBootApplication
@Configuration
@EnableConfigurationProperties
public class Application implements PromptProvider {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .logStartupInfo(true)
                .run(args);
    }

    private String connection;

    @Autowired
    @Lazy
    private History history;

    @Autowired
    private Shell shell;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return new ProvidedCommandLineRunner(shell);
    }

    @Bean
    @Lazy
    public History history(LineReader lineReader, @Value("${roachbank.history.file}") String historyPath) {
        lineReader.setVariable(LineReader.HISTORY_FILE, Paths.get(historyPath));
        return new DefaultHistory(lineReader);
    }

    @EventListener
    public void onContextClosedEvent(ContextClosedEvent event) throws IOException {
        history.save();
    }

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.connection = event.getBaseUri().getHost();
    }

    @Override
    public AttributedString getPrompt() {
        if (connection != null) {
            return new AttributedString(connection + ":$ ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        } else {
            return new AttributedString("disconnected:$ ", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        }
    }
}

@Order(InteractiveShellApplicationRunner.PRECEDENCE - 2)
class ProvidedCommandLineRunner implements CommandLineRunner {
    private final Shell shell;

    public ProvidedCommandLineRunner(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> argsList = new ArrayList<>();

        Arrays.stream(args).forEach(arg -> {
            String[] parts = arg.split(" ");
            argsList.addAll(Arrays.asList(parts));
        });

        List<String> block = new ArrayList<>();
        List<List<String>> commandBlocks = new ArrayList<>();

        argsList.forEach(arg -> {
            if ("&&".equals(arg) && !block.isEmpty()) {
                commandBlocks.add(new ArrayList<>(block));
                block.clear();
            } else if (!arg.startsWith("@")) {
                block.add(arg);
            }
        });

        if (!block.isEmpty()) {
            commandBlocks.add(block);
        }

        for (List<String> commandBlock : commandBlocks) {
            System.out.printf("Processing (%s):\n", StringUtils.collectionToDelimitedString(commandBlock, " "));
            shell.run(new StringInputProvider(commandBlock));
        }
    }
}

class StringInputProvider implements InputProvider {
    private final List<String> words;

    private boolean done;

    public StringInputProvider(List<String> words) {
        this.words = words;
    }

    @Override
    public Input readInput() {
        if (!done) {
            done = true;
            return new Input() {
                @Override
                public List<String> words() {
                    return words;
                }

                @Override
                public String rawText() {
                    return StringUtils.collectionToDelimitedString(words, " ");
                }
            };
        } else {
            return null;
        }
    }
}
