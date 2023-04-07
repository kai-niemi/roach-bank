package io.roach.bank.api;

import jakarta.persistence.Transient;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Set;

import static io.roach.bank.api.LinkRelations.*;

@Relation(value = CURIE_PREFIX + REGION_REL,
        collectionRelation = CURIE_PREFIX + REGION_LIST_REL)
public class Region {
    private String name;

    private List<String> cityGroups;

    @Transient
    private Set<String> cities;

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

    public List<String> getCityGroups() {
        return cityGroups;
    }

    public Region setCityGroups(List<String> cityGroups) {
        this.cityGroups = cityGroups;
        return this;
    }
}

