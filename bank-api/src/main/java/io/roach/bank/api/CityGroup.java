package io.roach.bank.api;

import org.springframework.hateoas.server.core.Relation;

import java.util.List;

import static io.roach.bank.api.LinkRelations.*;

@Relation(value = CURIE_PREFIX + CITY_GROUP_REL,
        collectionRelation = CURIE_PREFIX + CITY_GROUP_LIST_REL)
public class CityGroup {
    private String name;

    private List<String> cities;

    public String getName() {
        return name;
    }

    public CityGroup setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getCities() {
        return cities;
    }

    public CityGroup setCities(List<String> cities) {
        this.cities = cities;
        return this;
    }
}
