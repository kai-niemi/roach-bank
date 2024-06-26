package io.roach.bank.client.support;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.Region;
import io.roach.bank.client.Constants;
import io.roach.bank.client.event.ConnectionUpdatedEvent;

import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_TOP;
import static io.roach.bank.api.LinkRelations.CITY_LIST_REL;
import static io.roach.bank.api.LinkRelations.CONFIG_INDEX_REL;
import static io.roach.bank.api.LinkRelations.CONFIG_REGION_REL;
import static io.roach.bank.api.LinkRelations.GATEWAY_REGION_REL;
import static io.roach.bank.api.LinkRelations.REGION_LIST_REL;

public class HypermediaClient {
    private static final List<MediaType> ACCEPT_TYPES = List.of(MediaTypes.HAL_JSON);

    private final RestTemplate restTemplate;

    private URI baseUri;

    public HypermediaClient(RestTemplate restTemplate) {
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

    public <T> ResponseEntity<T> post(Link link, Class<T> responseType) {
        return restTemplate.postForEntity(
                link.getTemplate().expand(),
                null,
                responseType);
    }

    public <T> ResponseEntity<T> put(Link link, Class<T> responseType) {
        return restTemplate.exchange(
                link.getTemplate().expand(),
                HttpMethod.PUT,
                null,
                responseType);
    }

    public <T> ResponseEntity<T> delete(Link link, Class<T> responseType) {
        return restTemplate.exchange(
                link.getTemplate().expand(),
                HttpMethod.DELETE,
                null,
                responseType);
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
    public Collection<Region> getRegions() {
        TypeReferences.CollectionModelType<Region> collectionModelType =
                new TypeReferences.CollectionModelType<>() {
                };

        CollectionModel<Region> result = fromRoot()
                .follow(LinkRelations.withCurie(CONFIG_INDEX_REL))
                .follow(LinkRelations.withCurie(CONFIG_REGION_REL))
                .follow(LinkRelations.withCurie(REGION_LIST_REL))
                .toObject(collectionModelType);

        return result.getContent();
    }

    @SuppressWarnings("unchecked")
    public String getGatewayRegion() {
        Map<String, String> rv = fromRoot()
                .follow(LinkRelations.withCurie(CONFIG_INDEX_REL))
                .follow(LinkRelations.withCurie(CONFIG_REGION_REL))
                .follow(LinkRelations.withCurie(GATEWAY_REGION_REL))
                .toObject(Map.class);
        return rv.getOrDefault("region", "???");
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getRegionCities(String region) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("region", region);

        List<String> rv = fromRoot()
                .follow(LinkRelations.withCurie(CONFIG_INDEX_REL))
                .follow(LinkRelations.withCurie(CONFIG_REGION_REL))
                .follow(LinkRelations.withCurie(CITY_LIST_REL))
                .withTemplateParameters(parameters)
                .toObject(List.class);

        return rv;
    }

    public Map<String, List<AccountModel>> getTopAccounts(String region, int limit) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("region", region);
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
