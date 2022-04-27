package io.roach.bank.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MetadataRepository {
    Map<String, Set<String>> getRegionCities();

    Set<String> getRegionCities(Collection<String> regions);

    String getGatewayRegion();
}
