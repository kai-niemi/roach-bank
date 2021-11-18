package io.roach.bank.client.support;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TraversonHelper {
    public static final List<MediaType> ACCEPT_TYPES = Arrays.asList(MediaTypes.HAL_JSON);

    private URI baseUri;

    @Autowired
    @Qualifier("traversonRestTemplate")
    private RestTemplate restTemplate;

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.baseUri = event.getBaseUri();
    }

    public Traverson.TraversalBuilder follow(Optional<Link> link) {
        return from(link).follow();
    }

    public Traverson from(Optional<Link> link) {
        if (!link.isPresent()) {
            throw new IllegalStateException();
        }
        return from(link.get().toUri());
    }

    public Traverson fromRoot() {
        return from(baseUri);
    }

    public Traverson from(URI uri) {
        Traverson traverson = new Traverson(uri, ACCEPT_TYPES);
        traverson.setRestOperations(restTemplate);
        return traverson;
    }
}
