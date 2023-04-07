package io.roach.bank.repository.jdbc;

import io.roach.bank.domain.Region;
import io.roach.bank.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Array;
import java.util.*;

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
    public Region getRegion(String provider, String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("provider", provider);
        parameters.addValue("region", region);

        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM region WHERE region.provider_name=:provider AND region_name=:region",
                parameters,
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_names");
                    String[] cityGroups = (String[]) array.getArray();

                    Region r = new Region();
                    r.setProvider(provider);
                    r.setName(region);
                    r.setCityGroups(Arrays.asList(cityGroups));

                    r.setCities(listCityNamesByGroup(Arrays.asList(cityGroups)));

                    return r;
                });
    }

    @Override
    public Region addRegion(String provider, String region, List<String> cityGroups) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("provider", provider);
        parameters.addValue("region", region);
        parameters.addValue("cityGroups", cityGroups);

        namedParameterJdbcTemplate.update("INSERT INTO region (provider_name,region_name,city_groups) " +
                "VALUES (:provider,:region,:cityGroups)", parameters);

        Region r = new Region();
        r.setProvider(provider);
        r.setName(region);
        r.setCityGroups(cityGroups);
        r.setCities(listCityNamesByGroup(cityGroups));

        return r;
    }

    @Override
    public void deleteRegion(String provider, String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("provider", provider);
        parameters.addValue("region", region);
        namedParameterJdbcTemplate.update("DELETE FROM region WHERE provider_name=:provider AND region_name=:region",
                parameters);
    }

    @Override
    public List<Region> listRegions() {
        List<Region> regions = new ArrayList<>();

        this.namedParameterJdbcTemplate.query(
                "SELECT provider_name,region_name,city_groups FROM region ORDER BY region_name",
                Collections.emptyMap(),
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_groups");
                    String[] cityGroups = (String[]) array.getArray();

                    Region r = new Region();
                    r.setProvider(rs.getString("provider_name"));
                    r.setName(rs.getString("region_name"));
                    r.setCityGroups(Arrays.asList(cityGroups));

                    r.setCities(listCityNamesByGroup(Arrays.asList(cityGroups)));

                    regions.add(r);

                    return null;
                });

        return regions;
    }

    @Override
    public Set<String> listCities(Collection<String> regions) {
        List<String> regionList = new ArrayList<>(regions);
        if (regionList.isEmpty()) {
            regionList.add("*"); // Include all if empty
        }

        Set<String> cities = new TreeSet<>();

        regionList.forEach(region -> {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("region", region.replace("*", "%"));

            this.namedParameterJdbcTemplate.query(
                    "SELECT city_groups FROM region where region_name like :region",
                    parameters,
                    (rs, rowNum) -> {
                        Array array = rs.getArray("city_groups");
                        String[] cityGroups = (String[]) array.getArray();
                        cities.addAll(listCityNamesByGroup(Arrays.asList(cityGroups)));
                        return null;
                    });
        });

        return cities;
    }

    private Set<String> listCityNamesByGroup(Collection<String> groupNames) {
        Set<String> cities = new TreeSet<>();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("names", groupNames);

        this.namedParameterJdbcTemplate.query(
                "select array_cat_agg(city_names) city_names from city where name in (:names)",
                parameters,
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_names");
                    String[] cityGroups = (String[]) array.getArray();
                    cities.addAll(Arrays.asList(cityGroups));
                    return null;
                });

        return cities;
    }

    @Override
    public String getGatewayRegion() {
        try {
            return namedParameterJdbcTemplate
                    .queryForObject("SELECT region_name FROM region WHERE region_name=gateway_region()",
                            Collections.emptyMap(),
                            String.class);
        } catch (EmptyResultDataAccessException e) {
            return defaultGatewayRegion;
        }
    }

    @Override
    public List<Region> listDatabaseRegions() {
        List<Region> regions = new ArrayList<>();

        this.namedParameterJdbcTemplate.query(
                "SELECT region FROM [SHOW regions]",
                Collections.emptyMap(),
                (rs, rowNum) -> {
                    Region r = new Region();
                    r.setProvider("(n/a)");
                    r.setName(rs.getString("region"));

                    regions.add(r);

                    return null;
                });

        return regions;
    }
}
