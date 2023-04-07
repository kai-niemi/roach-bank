package io.roach.bank.repository;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.Region;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MetadataRepository {
    Region getRegionByName(String region);

    Region createRegion(Region region);

    List<Region> listRegions();

    List<CityGroup> listCityGroups();

    Set<String> listCities(Collection<String> regions);

    String getGatewayRegion();

    List<Region> listDatabaseRegions();

    void syncRegions();

    boolean doesAccountPlanExist();
}
