package io.roach.bank.client.command;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import io.roach.bank.client.support.ConnectionUpdatedEvent;
import io.roach.bank.client.support.Console;

@ShellComponent
@ShellCommandGroup(Constants.API_MAIN_COMMANDS)
public class Connect {
    public static final String DEFAULT_URL = "http://localhost:8090/api/";

    private static boolean connected;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Console console;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public static boolean isConnected() {
        return connected;
    }

    @ShellMethod(value = "Connect to API endpoint", key = {"connect", "c"})
    public void connect(@ShellOption(value = {"--url", "-u"},
            help = "REST API base URI", defaultValue = DEFAULT_URL) String baseUrl) {

        console.debug("Connecting to %s..", baseUrl);

        ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl, String.class);

        if (entity.getStatusCode() == HttpStatus.OK) {
            Connect.connected = true;

            ResponseEntity<String> response = new Traverson(URI.create(baseUrl), MediaType.APPLICATION_JSON)
                    .follow().toEntity(String.class);

            String name = response.getHeaders().toSingleValueMap().get("X-Application-Context");
            if ("Roach Bank".equals(name)) {
                String message = JsonPath.parse(response.getBody()).read("$.message", String.class);
                console.info(message);

                applicationEventPublisher.publishEvent(
                        new ConnectionUpdatedEvent(this,
                                URI.create(baseUrl),
                                entity.getStatusCode()
                        ));
            } else {
                console.warn("This doesnt look like Roach Bank API - please check URL!");
                console.info(response.getBody());
            }
        } else {
            console.error("Connection failed (%s)", entity.getStatusCode().getReasonPhrase());
        }
    }
}
