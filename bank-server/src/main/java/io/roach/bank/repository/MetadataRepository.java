package io.roach.bank.repository;

import io.roach.bank.domain.Region;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MetadataRepository {
    Region getRegion(String provider, String region);

    Region addRegion(String provider, String region, List<String> cityGroups);

    void deleteRegion(String provider, String region);

    List<Region> listRegions();

    Set<String> listCities(Collection<String> regions);

    String getGatewayRegion();

    List<Region> listDatabaseRegions();
}
