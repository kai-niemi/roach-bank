package io.roach.bank.repository.jdbc;

import java.util.*;

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
    public Set<Currency> getCurrencies() {
        return new HashSet<>(this.namedParameterJdbcTemplate.query(
                "SELECT DISTINCT currency FROM city",
                (rs, rowNum) -> Currency.getInstance(rs.getString(1))));
    }

    @Override
    public Map<String, Currency> getCities() {
        Map<String, Currency> result = new HashMap<>();

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        this.namedParameterJdbcTemplate.query(
                "SELECT name,currency FROM city GROUP BY name,currency",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), Currency.getInstance(rs.getString(2)));
                    return null;
                });

        return result;
    }

    @Override
    public Map<String, String> getRegions() {
        Map<String, String> result = new HashMap<>();

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        this.namedParameterJdbcTemplate.query(
                "SELECT name,cities FROM region",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), rs.getString(2));
                    return null;
                });

        return result;
    }

    @Override
    public Set<String> getRegionCities() {
        return getRegionCities(new ArrayList<>());
    }

    @Override
    public Set<String> getRegionCities(Collection<String> regions) {
        if (regions.isEmpty()) {
            regions.add(getGatewayRegion());
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("regions", regions);

        Set<String> cities = new HashSet<>();

        this.namedParameterJdbcTemplate.query(
                "SELECT cities FROM region where name in (:regions)",
                parameters,
                (rs, rowNum) -> {
                    String v = rs.getString(1);
                    cities.addAll(StringUtils.commaDelimitedListToSet(v));
                    return null;
                });

        return cities;
    }

    @Override
    public String getGatewayRegion() {
        return namedParameterJdbcTemplate
                .queryForObject("SELECT gateway_region()", Collections.emptyMap(), String.class);
    }
}
