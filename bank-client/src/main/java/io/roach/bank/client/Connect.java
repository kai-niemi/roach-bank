package io.roach.bank.client;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import io.roach.bank.client.event.ConnectionUpdatedEvent;

@ShellComponent
@ShellCommandGroup(Constants.WORKLOAD_COMMANDS)
public class Connect extends AbstractCommand {
    private static boolean connected;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${roachbank.default-url}")
    private String defaultUrl;

    public static boolean isConnected() {
        return connected;
    }

    @ShellMethod(value = "Connect to API endpoint", key = {"connect", "c"})
    public void connect(@ShellOption(value = {"--url", "-u"},
            help = "REST API base URI (default is http://localhost:8090/api)",
            defaultValue = ShellOption.NULL) String baseUrl) {
        if (!StringUtils.hasLength(baseUrl)) {
            baseUrl = defaultUrl;
        }

        logger.info("Connecting to {}..", baseUrl);

        ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl, String.class);

        if (entity.getStatusCode() == HttpStatus.OK) {
            Connect.connected = true;

            ResponseEntity<String> response = new Traverson(URI.create(baseUrl), MediaType.APPLICATION_JSON)
                    .follow().toEntity(String.class);

            String name = response.getHeaders().toSingleValueMap().get("X-Application-Context");
            if ("Roach Bank".equals(name)) {
                String message = JsonPath.parse(response.getBody()).read("$.message", String.class);

                logger.info(message);
                logger.info("Type help for commands.");

                applicationEventPublisher.publishEvent(
                        new ConnectionUpdatedEvent(this,
                                URI.create(baseUrl),
                                entity.getStatusCode()
                        ));
            } else {
                logger.warn("This doesnt look like Roach Bank API - please check URL!");
                logger.warn(response.getBody());
            }
        } else {
            logger.error("Connection failed: {}", entity.getStatusCode());
        }
    }
}
