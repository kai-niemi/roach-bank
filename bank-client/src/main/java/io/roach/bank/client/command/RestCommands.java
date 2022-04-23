package io.roach.bank.client.command;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.Assert;

import io.roach.bank.api.AccountModel;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.client.support.TraversonHelper;

import static io.roach.bank.api.LinkRelations.ACCOUNT_REL;
import static io.roach.bank.api.LinkRelations.ACCOUNT_TOP;
import static io.roach.bank.api.LinkRelations.CITIES_REL;
import static io.roach.bank.api.LinkRelations.META_REL;
import static io.roach.bank.api.LinkRelations.REGIONS_REL;
import static io.roach.bank.api.LinkRelations.REGION_CITIES_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

public class RestCommands {
    private TraversonHelper traversonHelper;

    public RestCommands(TraversonHelper traversonHelper) {
        this.traversonHelper = traversonHelper;
    }

    public Set<String> getRegions() {
        Set<String> result = traversonHelper.fromRoot()
                .follow(LinkRelations.withCurie(META_REL))
                .follow(LinkRelations.withCurie(REGIONS_REL))
                .toObject(Set.class);
        return result;
    }

    public Set<String> getCities() {
        return getCityCurrency().keySet();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Currency> getCityCurrency() {
        final Map<String, Object> parameters = new HashMap<>();
        final Map<String, Currency> result = new HashMap<>();

        Objects.requireNonNull(traversonHelper.fromRoot()
                .follow(withCurie(META_REL))
                .follow(withCurie(CITIES_REL))
                .withTemplateParameters(parameters)
                .toObject(Map.class))
                .forEach((k, v) -> result.put(
                        (String) k, Currency.getInstance((String) v))
                );

        return result;
    }

    public Set<String> getRegionCities(String regions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("regions", regions);

        final Set<String> cities = new HashSet<>();

        Objects.requireNonNull(traversonHelper.fromRoot()
                .follow(LinkRelations.withCurie(META_REL))
                .follow(LinkRelations.withCurie(REGION_CITIES_REL))
                .withTemplateParameters(parameters)
                .toObject(Set.class))
                .forEach(city -> cities.add((String) city));

        return cities;
    }

    public Map<String, java.util.List<AccountModel>> getCityAccounts(String alias, int accountLimit) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("cities", alias);
        parameters.put("limit", accountLimit);

        final Map<String, List<AccountModel>> accountMap = new HashMap<>();

        for (AccountModel account : Objects.requireNonNull(traversonHelper.fromRoot()
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
