package io.roach.bank.api;

import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Set;

import static io.roach.bank.api.LinkRelations.CURIE_PREFIX;
import static io.roach.bank.api.LinkRelations.REGION_LIST_REL;
import static io.roach.bank.api.LinkRelations.REGION_REL;

@Relation(value = CURIE_PREFIX + REGION_REL,
        collectionRelation = CURIE_PREFIX + REGION_LIST_REL)
public class Region implements Comparable<Region> {
    private String name;

    private Set<String> cities;

    private String databaseRegion;

    private boolean primary;

    public boolean isPrimary() {
        return primary;
    }

    public Region setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }

    public String getDatabaseRegion() {
        return databaseRegion;
    }

    public Region setDatabaseRegion(String databaseRegion) {
        this.databaseRegion = databaseRegion;
        return this;
    }

    public String getName() {
        return name;
    }

    public Region setName(String name) {
        this.name = name;
        return this;
    }

    public Set<String> getCities() {
        return cities;
    }

    public Region setCities(Set<String> cities) {
        this.cities = cities;
        return this;
    }

    @Override
    public int compareTo(Region o) {
        return name.compareTo(o.name);
    }
}

