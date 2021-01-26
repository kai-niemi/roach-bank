package io.roach.bank.client.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.shell.Availability;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.client.support.ConnectionUpdatedEvent;
import io.roach.bank.client.support.Console;
import io.roach.bank.client.support.ThrottledExecutor;
import io.roach.bank.client.support.TraversonHelper;

import static io.roach.bank.api.BankLinkRelations.*;

public abstract class RestCommandSupport {
    @Autowired
    protected Console console;

    @Autowired
    protected TraversonHelper traverson;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ThrottledExecutor throttledExecutor;

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
    protected Map<String, Currency> lookupRegions(String regions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("regions", regions);

        Map<String, Currency> result = new HashMap<>();

        Objects.requireNonNull(traverson.fromRoot()
                .follow(withCurie(META_REL))
                .follow(withCurie(REGION_CURRENCY_REL))
                .withTemplateParameters(parameters)
                .toObject(Map.class))
                .forEach((k, v) -> result.put(
                        (String) k, Currency.getInstance((String) v))
                );


        if (result.isEmpty()) {
            console.warn("No matching regions for: %s ", regions);
        }

        return result;
    }

    protected Map<String, List<AccountModel>> lookupAccounts(Set<String> regions, int limit) {
        console.debug("Looking up top %d accounts in regions %s", limit, regions);

        final Map<String, List<AccountModel>> accountMap = new HashMap<>();

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", limit);
        parameters.put("regions", StringUtils.collectionToCommaDelimitedString(regions));

        // Get top accounts, filter client-side based on region
        for (AccountModel account : Objects.requireNonNull(traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(ACCOUNT_REL))
                .follow(BankLinkRelations.withCurie(ACCOUNT_TOP))
                .withTemplateParameters(parameters)
                .toObject(Constants.ACCOUNT_MODEL_PTR))) {
            Assert.isTrue(regions.contains(account.getRegion()), "region mismatch!");
            accountMap.computeIfAbsent(account.getRegion(), l -> new ArrayList<>()).add(account);
        }

        if (accountMap.isEmpty()) {
            console.warn("No accounts in regions %s ", regions);
        }

        return accountMap;
    }

}
