package io.roach.bank.domain;

import jakarta.persistence.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.Set;

@Table("region")
public class Region {
    @Column
    private String name;

    @Column
    private String provider;

    @Column
    private List<String> cityGroups;

    @Transient
    private Set<String> cities;

    public String getProvider() {
        return provider;
    }

    public Region setProvider(String provider) {
        this.provider = provider;
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

    public List<String> getCityGroups() {
        return cityGroups;
    }

    public Region setCityGroups(List<String> cityGroups) {
        this.cityGroups = cityGroups;
        return this;
    }
}

