package io.roach.bank.repository;

import io.roach.bank.api.Region;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RegionRepository {
    List<Region> listRegions(Collection<String> regions);

    Set<String> listCities(Collection<Region> regions);

    Region getRegionByName(String region);

    String getGatewayRegion();

    Optional<String> getPrimaryRegion();

    Optional<String> getSecondaryRegion();

    Boolean hasExistingAccountPlan();

    void createRegion(Region region);

    void createRegionMappings(Map<String, String> mappings);
}
