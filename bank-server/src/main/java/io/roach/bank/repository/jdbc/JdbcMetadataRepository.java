package io.roach.bank.repository.jdbc;

import java.util.*;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.roach.bank.domain.Region;
import io.roach.bank.repository.MetadataRepository;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class JdbcMetadataRepository implements MetadataRepository {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${roachbank.default-gateway-region}")
    private String defaultGatewayRegion;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Map<String, List<Region>> getAllRegions() {
        Map<String, List<Region>> result = new TreeMap<>();

        List<String> clouds = namedParameterJdbcTemplate
                .queryForList("SELECT cloud FROM region GROUP BY cloud", Collections.emptyMap(), String.class);

        clouds.forEach(cloud -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("cloud", cloud);

            this.namedParameterJdbcTemplate.query(
                    "SELECT name,cities FROM region WHERE cloud=:cloud ORDER BY name",
                    parameters,
                    (rs, rowNum) -> {
                        Region r = new Region();
                        r.setCloud(cloud);
                        r.setName(rs.getString(1));
                        r.setCities(StringUtils.commaDelimitedListToSet(rs.getString(2)));
                        result.computeIfAbsent(cloud, regions -> new ArrayList<>()).add(r);
                        return null;
                    });
        });

        return result;
    }

    @Override
    public Map<String, Set<String>> getAllRegionCities() {
        Map<String, Set<String>> result = new TreeMap<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        this.namedParameterJdbcTemplate.query(
                "SELECT name,cities FROM region",
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
            regionList.add("*"); // Include all if empty
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

    @Override
    public String getDefaultGatewayRegion() {
        try {
            return namedParameterJdbcTemplate
                    .queryForObject("SELECT name FROM region WHERE name=gateway_region()",
                            Collections.emptyMap(),
                            String.class);

        } catch (EmptyResultDataAccessException e) {
            return defaultGatewayRegion;
        }
    }
}
