package io.roach.bank.repository;

import java.util.Collection;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

public interface MetadataRepository {
    Set<String> getRegions();

    Map<String, Currency> getCities();

    Set<Currency> getCurrencies();

    Set<String> getRegionCities(Collection<String> regions);

    String getGatewayRegion();
}
