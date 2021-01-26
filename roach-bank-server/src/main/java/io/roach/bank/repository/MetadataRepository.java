package io.roach.bank.repository;

import java.util.Currency;
import java.util.List;
import java.util.Map;

public interface MetadataRepository {
    List<String> getLocalRegions();

    List<String> getRegions();

    List<String> getGroups();

    List<Currency> getCurrencies();

    Currency getRegionCurrency(String region);

    Map<String, List<String>> getGroupRegions();

    Map<Currency, List<String>> getCurrencyRegions();

    Map<String, Currency> resolveRegions(List<String> regions);
}
