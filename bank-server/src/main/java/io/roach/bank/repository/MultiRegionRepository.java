package io.roach.bank.repository;

import java.util.List;

import io.roach.bank.api.Region;
import io.roach.bank.domain.SurvivalGoal;

public interface MultiRegionRepository {
    void addDatabaseRegions(List<Region> regions);

    void dropDatabaseRegions(List<Region> regions);

    void setPrimaryRegion(Region region);

    void setSecondaryRegion(Region region);

    void dropSecondaryRegion();

    void setSurvivalGoal(SurvivalGoal survivalGoal);

    void addGlobalTable(String table);

    void addRegionalByRowTable(String table);

    void addRegionalTable(String table);
}
