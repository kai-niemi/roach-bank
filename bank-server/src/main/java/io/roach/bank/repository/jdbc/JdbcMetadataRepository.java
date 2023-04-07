package io.roach.bank.repository.jdbc;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.Region;
import io.roach.bank.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Region getRegionByName(String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", region);

        return this.jdbcTemplate.queryForObject(
                "SELECT city_groups FROM region WHERE region_name=:region",
                parameters,
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_groups");
                    String[] cityGroups = (String[]) array.getArray();

                    Region r = new Region();
                    r.setName(region);
                    r.setCityGroups(Arrays.asList(cityGroups));
                    r.setCities(listCityNamesByGroup(Arrays.asList(cityGroups)));

                    return r;
                });
    }

    @Override
    public Region createRegion(Region region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", region.getName());
        parameters.addValue("cityGroups", region.getCityGroups());

        jdbcTemplate.update("INSERT INTO region (region_name,city_groups) " +
                "VALUES (:region,:cityGroups)", parameters);

        region.setCities(listCityNamesByGroup(region.getCityGroups()));

        return region;
    }

    public void deleteRegion(String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", region);

        jdbcTemplate.update("DELETE FROM region WHERE region_name=:region",
                parameters);
    }

    @Override
    public List<Region> listRegions() {
        List<Region> regions = new ArrayList<>();

        this.jdbcTemplate.query(
                "SELECT region_name,city_groups FROM region ORDER BY region_name",
                Collections.emptyMap(),
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_groups");
                    String[] cityGroups = (String[]) array.getArray();

                    Region r = new Region();
                    r.setName(rs.getString("region_name"));
                    r.setCityGroups(Arrays.asList(cityGroups));

                    r.setCities(listCityNamesByGroup(Arrays.asList(cityGroups)));

                    regions.add(r);

                    return null;
                });

        return regions;
    }

    @Override
    public List<CityGroup> listCityGroups() {
        List<CityGroup> groups = new ArrayList<>();

        this.jdbcTemplate.query(
                "SELECT name,city_names FROM city_group ORDER BY name",
                Collections.emptyMap(),
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_names");
                    String[] cityNames = (String[]) array.getArray();

                    CityGroup group = new CityGroup();
                    group.setName(rs.getString("name"));
                    group.setCities(Arrays.asList(cityNames));

                    groups.add(group);

                    return null;
                });

        return groups;
    }

    private List<String> listCityGroupNamess() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        return jdbcTemplate.queryForList(
                "SELECT name FROM city_group ORDER BY name",
                parameters,
                String.class);
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

            this.jdbcTemplate.query(
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

        this.jdbcTemplate.query(
                "select array_cat_agg(city_names) city_names from city_group where name in (:names)",
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
        return this.jdbcTemplate
                .queryForObject("SELECT region_name FROM region WHERE region_name=gateway_region()",
                        Collections.emptyMap(),
                        String.class);
    }

    @Override
    public List<Region> listDatabaseRegions() {
        List<Region> regions = new ArrayList<>();

        this.jdbcTemplate.query(
                "SELECT region FROM [SHOW regions]",
                Collections.emptyMap(),
                (rs, rowNum) -> {
                    regions.add(getRegionByName(rs.getString("region")));
                    return null;
                });

        return regions;
    }

    @Override
    public boolean doesAccountPlanExist() {
        return this.jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM account LIMIT 1)",
                Collections.emptyMap(),
                Boolean.class);
    }

    @Override
    public void syncRegions() {
        List<String> cityGroups = listCityGroupNamess();

        listDatabaseRegions().forEach(region -> {
            Region r = getRegionByName(region.getName());
            if (r == null) {
                createRegion(new Region()
                        .setName(r.getName())
                        .setCityGroups(cityGroups)
                );
            }
        });
    }
}
