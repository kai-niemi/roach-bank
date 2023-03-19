package io.roach.bank.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.roach.bank.domain.Region;

public interface MetadataRepository {
    Map<String, List<Region>> getAllRegions();

    Map<String, Set<String>> getAllRegionCities();

    Set<String> getRegionCities(Collection<String> regions);

    String getDefaultGatewayRegion();
}
