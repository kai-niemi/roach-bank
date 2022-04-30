package io.roach.bank.domain;

import java.util.Set;

public class Region {
    private String cloud;

    private String name;

    private Set<String> cities;

    public String getCloud() {
        return cloud;
    }

    public Region setCloud(String cloud) {
        this.cloud = cloud;
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
}
