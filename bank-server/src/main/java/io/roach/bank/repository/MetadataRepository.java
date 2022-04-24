package io.roach.bank.repository;

import java.util.Collection;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

public interface MetadataRepository {
    Map<String, String> getRegions();

    Map<String, Currency> getCities();

    Set<Currency> getCurrencies();

    Set<String> getRegionCities();

    Set<String> getRegionCities(Collection<String> regions);

    String getGatewayRegion();
}
