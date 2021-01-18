package io.roach.bank.repository;

import java.util.Currency;
import java.util.List;
import java.util.Map;

import io.roach.bank.api.RegionConfig;

public interface MetadataRepository {
    List<String> getRegions();

    List<String> getGroups();

    List<Currency> getCurrencies();

    Currency getRegionCurrency(String region);

    Map<String, List<String>> getGroupRegions();

    Map<Currency, List<String>> getCurrencyRegions();

    List<RegionConfig> getRegionConfigs();

    Map<String, Currency> resolveRegions(List<String> regions);
}
