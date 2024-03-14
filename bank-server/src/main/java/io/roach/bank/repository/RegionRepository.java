package io.roach.bank.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.Region;

public interface RegionRepository {
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

    boolean hasAccountPlan();
}
