package io.roach.bank.repository;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetadataRepository {
    Set<Currency> getCurrencies();

    Set<String> getRegions();

    Set<String> getRegionCities(Collection<String> regions);

    Map<String, List<String>> getRegionToCityMap();

    Map<Currency, List<String>> getCurrencyToCityMap();

    Map<String, Currency> getCityToCurrencyMap();

    Currency getCityCurrency(String city);
}
