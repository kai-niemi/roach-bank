package io.roach.bank.client.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.shell.Availability;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.client.support.ExecutorTemplate;
import io.roach.bank.client.support.ConnectionUpdatedEvent;
import io.roach.bank.client.support.TraversonHelper;

import static io.roach.bank.api.BankLinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.BankLinkRelations.ACCOUNT_TOP;
import static io.roach.bank.api.BankLinkRelations.META_REL;
import static io.roach.bank.api.BankLinkRelations.REGION_CURRENCIES_REL;
import static io.roach.bank.api.BankLinkRelations.withCurie;

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
    protected Map<String, Currency> lookupRegions(String regions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("regions", regions);

        Map<String, Currency> result = new HashMap<>();

        Objects.requireNonNull(traverson.fromRoot()
                .follow(withCurie(META_REL))
                .follow(withCurie(REGION_CURRENCIES_REL))
                .withTemplateParameters(parameters)
                .toObject(Map.class))
                .forEach((k, v) -> result.put(
                        (String) k, Currency.getInstance((String) v))
                );


        if (result.isEmpty()) {
            logger.warn("No matching regions: {}", regions);
        } else {
            logger.info("Region currencies: {}", result);
        }

        return result;
    }

    protected Map<String, List<AccountModel>> lookupAccounts(Set<String> regions, int limit) {
        final Map<String, List<AccountModel>> accountMap = new HashMap<>();
        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("limit", limit);
        parameters.put("regions", StringUtils.collectionToCommaDelimitedString(regions));

        logger.info("Looking up top accounts in {} (limit {})",
                StringUtils.collectionToCommaDelimitedString(regions), limit);

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
            logger.warn("No accounts in regions [{}]", regions);
        } else {
            logger.info("Accounts in regions [{}]", regions);
            accountMap.forEach((r, accountModels) -> {
                logger.info("{} ({} accounts)", r, accountModels.size());
            });
        }

        return accountMap;
    }

}
