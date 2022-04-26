package io.roach.bank.repository;

import java.util.Collection;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

public interface MetadataRepository {
    Set<Currency> getCurrencies();

    Map<String, Set<String>> getAllRegionCities();

    Set<String> getRegionCities(Collection<String> regions);

    String getGatewayRegion();
}
