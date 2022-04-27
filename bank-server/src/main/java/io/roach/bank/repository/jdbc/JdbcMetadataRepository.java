package io.roach.bank.repository.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import io.roach.bank.repository.MetadataRepository;

@Repository
public class JdbcMetadataRepository implements MetadataRepository {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Map<String, Set<String>> getRegionCities() {
        Map<String, Set<String>> result = new HashMap<>();

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        this.namedParameterJdbcTemplate.query(
                "SELECT name,cities FROM region ORDER BY name",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), StringUtils.commaDelimitedListToSet(rs.getString(2)));
                    return null;
                });

        return result;
    }

    @Override
    public Set<String> getRegionCities(Collection<String> regions) {
        List<String> regionList = new ArrayList<>(regions);
        if (regionList.isEmpty()) {
            regionList.addAll(getGatewayRegions());
        }

        Set<String> cities = new TreeSet<>();

        regionList.forEach(region -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("region", region.replace("*", "%"));

            this.namedParameterJdbcTemplate.query(
                    "SELECT cities FROM region where name like :region",
                    parameters,
                    (rs, rowNum) -> {
                        String v = rs.getString(1);
                        cities.addAll(StringUtils.commaDelimitedListToSet(v));
                        return null;
                    });
        });

        return cities;
    }

    private Set<String> getGatewayRegions() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        Set<String> regions = new HashSet<>();

        this.namedParameterJdbcTemplate.query(
                "SELECT region FROM cloud_region where name=(SELECT gateway_region())",
                parameters,
                (rs, rowNum) -> {
                    String v = rs.getString(1);
                    regions.addAll(StringUtils.commaDelimitedListToSet(v));
                    return null;
                });

        return regions;
    }

    @Override
    public String getGatewayRegion() {
        return namedParameterJdbcTemplate
                .queryForObject("SELECT gateway_region()", Collections.emptyMap(), String.class);
    }
}
