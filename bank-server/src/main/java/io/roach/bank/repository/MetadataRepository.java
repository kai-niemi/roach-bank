package io.roach.bank.repository;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public interface MetadataRepository {
    List<String> getCities();

    List<String> getLocalCities();

    List<Currency> getCurrencies();

    List<String> getRegions();

    Map<String, List<String>> getRegionCities();

    Map<Currency, List<String>> getCurrencyCities();

    Map<String, Currency> getCityCurrency(Collection<String> cities);

    Currency getCityCurrency(String city);
}
