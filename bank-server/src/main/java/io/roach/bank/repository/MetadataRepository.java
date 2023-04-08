package io.roach.bank.repository;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.Region;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface MetadataRepository {
    List<Region> listRegions();

    List<CityGroup> listCityGroups();

    Set<String> listCities(Collection<String> regions);

    Region getRegionByName(String region);

    Region createRegion(Region region);

    Region updateRegion(Region region);

    void deleteRegion(String region);

    CityGroup getCityGroup(String name);

    CityGroup updateCityGroup(CityGroup cityGroup);

    String getGatewayRegion();

    boolean doesAccountPlanExist();
}
