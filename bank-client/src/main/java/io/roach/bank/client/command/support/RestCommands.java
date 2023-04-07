package io.roach.bank.client.command.support;

import java.net.URI;
import java.util.*;

import org.springframework.context.event.EventListener;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.client.command.Constants;

import static io.roach.bank.api.LinkRelations.*;

public class RestCommands {
    private static final List<MediaType> ACCEPT_TYPES = Arrays.asList(MediaTypes.HAL_JSON);

    private final RestTemplate restTemplate;

    private URI baseUri;

    public RestCommands(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.baseUri = event.getBaseUri();
    }

    public Traverson fromRoot() {
        return from(baseUri);
    }

    public Traverson.TraversalBuilder follow(Optional<Link> link) {
        return from(link.get().toUri()).follow();
    }

    private Traverson from(URI uri) {
        Traverson traverson = new Traverson(uri, ACCEPT_TYPES);
        traverson.setRestOperations(restTemplate);
        return traverson;
    }

    public ResponseEntity<String> post(Link link) {
        return restTemplate.postForEntity(link.getTemplate().expand(), null, String.class);
    }

    public <T> ResponseEntity<T> post(Link link, Class<T> reponseType) {
        return restTemplate.postForEntity(
                link.getTemplate().expand(),
                null,
                reponseType);
    }

    public <T> ResponseEntity<T> post(Link link, Object request, Class<T> reponseType) {
        return restTemplate.postForEntity(
                link.getTemplate().expand(),
                request,
                reponseType);
    }

    public ResponseEntity<String> get(Link link) {
        return restTemplate.getForEntity(link.toUri(), String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRegions() {
        List<String> result = fromRoot()
                .follow(LinkRelations.withCurie(META_REL))
                .follow(LinkRelations.withCurie(REGION_LIST_REL))
                .toObject(List.class);
        return result;
    }

    @SuppressWarnings("unchecked")
    public String getGatewayRegion() {
        return fromRoot()
                .follow(LinkRelations.withCurie(META_REL))
                .follow(LinkRelations.withCurie(GATEWAY_REGION_REL))
                .toObject(String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRegionCities(Set<String> regions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("regions", regions);

        final Set<String> cities = new HashSet<>();

        Objects.requireNonNull(fromRoot()
                        .follow(LinkRelations.withCurie(META_REL))
                        .follow(LinkRelations.withCurie(REGION_CITIES_REL))
                        .withTemplateParameters(parameters)
                        .toObject(Set.class))
                .forEach(city -> cities.add((String) city));

        return cities;
    }

    public Map<String, List<AccountModel>> getTopAccounts(Set<String> regions, int limit) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("regions", regions);
        parameters.put("limit", limit);

        final Map<String, List<AccountModel>> accounts = new HashMap<>();

        for (AccountModel account : Objects.requireNonNull(fromRoot()
                .follow(LinkRelations.withCurie(ACCOUNT_REL))
                .follow(LinkRelations.withCurie(ACCOUNT_TOP))
                .withTemplateParameters(parameters)
                .toObject(Constants.ACCOUNT_MODEL_PTR))) {
            accounts.computeIfAbsent(account.getCity(), l -> new ArrayList<>()).add(account);
        }

        return accounts;
    }
}
