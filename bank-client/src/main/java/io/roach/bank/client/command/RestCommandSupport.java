package io.roach.bank.client.command;

import java.net.URI;
import java.util.List;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.shell.Availability;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.client.support.ConnectionUpdatedEvent;
import io.roach.bank.client.support.ExecutorTemplate;
import io.roach.bank.client.support.TraversonHelper;

import static io.roach.bank.api.LinkRelations.*;

public abstract class RestCommandSupport {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected TraversonHelper traverson;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ExecutorTemplate executorTemplate;

    protected URI baseUri;

    @EventListener
    public void handle(ConnectionUpdatedEvent event) {
        this.baseUri = event.getBaseUri();
    }

    public Availability connectedCheck() {
        return Connect.isConnected()
                ? Availability.available()
                : Availability.unavailable("You are not connected");
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Currency> getCityCurrencyMap() {
        final Map<String, Object> parameters = new HashMap<>();
        final Map<String, Currency> result = new HashMap<>();

        Objects.requireNonNull(traverson.fromRoot()
                        .follow(withCurie(META_REL))
                        .follow(withCurie(CITIES_REL))
                        .withTemplateParameters(parameters)
                        .toObject(Map.class))
                .forEach((k, v) -> result.put(
                        (String) k, Currency.getInstance((String) v))
                );

        return result;
    }

    protected Set<String> getRegionCities(String regions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("regions", regions);

        final Set<String> cities = new HashSet<>();

        Objects.requireNonNull(traverson.fromRoot()
                        .follow(LinkRelations.withCurie(META_REL))
                        .follow(LinkRelations.withCurie(REGION_CITIES_REL))
                        .withTemplateParameters(parameters)
                        .toObject(Set.class))
                .forEach(city -> cities.add((String) city));

        return cities;
    }

    protected Map<String, List<AccountModel>> getCityAccounts(String alias, int accountLimit) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("cities", alias);
        parameters.put("limit", accountLimit);

        final Map<String, List<AccountModel>> accountMap = new HashMap<>();

        for (AccountModel account : Objects.requireNonNull(traverson.fromRoot()
                .follow(LinkRelations.withCurie(ACCOUNT_REL))
                .follow(LinkRelations.withCurie(ACCOUNT_TOP))
                .withTemplateParameters(parameters)
                .toObject(Constants.ACCOUNT_MODEL_PTR))) {
            Assert.isTrue(alias.contains(account.getCity()), "city mismatch!");
            accountMap.computeIfAbsent(account.getCity(), l -> new ArrayList<>()).add(account);
        }

        return accountMap;
    }
}
