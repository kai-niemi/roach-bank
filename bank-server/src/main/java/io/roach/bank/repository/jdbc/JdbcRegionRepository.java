package io.roach.bank.repository.jdbc;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.Region;
import io.roach.bank.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class JdbcRegionRepository implements RegionRepository {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public List<Region> listRegions() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT region,(primary_region_of[1]='roach_bank') as primary FROM [SHOW regions] ORDER BY region",
                Collections.emptyMap(),
                (rs, rowNum) -> {
                    Region r = getOrCreateRegionByName(rs.getString("region"));
                    r.setPrimary(rs.getBoolean(2));
                    return r;
                });
    }

    @Override
    public List<CityGroup> listCityGroups() {
        List<CityGroup> groups = new ArrayList<>();

        this.namedParameterJdbcTemplate.query(
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

    @Override
    public Set<String> listCities(Collection<String> regions) {
        Set<String> cities = new TreeSet<>();

        if (regions.isEmpty()) {
            listRegions().forEach(region -> cities.addAll(region.getCities()));
        } else {
            regions.forEach(region -> cities.addAll(findRegionByName(region).getCities()));
        }

        return cities;
    }

    @Override
    public Region getOrCreateRegionByName(String region) {
        try {
            return findRegionByName(region);
        } catch (EmptyResultDataAccessException e) {
            List<String> groups = findRegionByName("default").getCityGroups();
            return createRegion(new Region()
                    .setName(region)
                    .setCityGroups(groups)
                    .setCities(listCityNames(groups)));
        }
    }

    private Region findRegionByName(String region) throws EmptyResultDataAccessException {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", region);

        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT name,city_groups FROM region WHERE name = :name",
                parameters,
                (rs, rowNum) -> {
                    Array array = rs.getArray("city_groups");
                    String[] cityGroups = (String[]) array.getArray();

                    Region r = new Region();
                    r.setName(rs.getString("name"));
                    r.setCityGroups(Arrays.asList(cityGroups));
                    r.setCities(listCityNames(Arrays.asList(cityGroups)));

                    return r;
                });
    }

    @Override
    public Region createRegion(Region region) {
        String sql = "INSERT INTO region (name,city_groups) VALUES (?,?)";

        int rows = this.jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, region.getName());
            ps.setArray(2, conn.createArrayOf("VARCHAR", region.getCityGroups().toArray()));
            return ps;
        });
        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, 0);
        }

        return region;
    }

    @Override
    public Region updateRegion(Region region) {
        String sql = "UPDATE region SET city_groups=? WHERE name=?";
        int rows = this.jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setArray(1, conn.createArrayOf("VARCHAR", region.getCityGroups().toArray()));
            ps.setString(2, region.getName());
            return ps;
        });
        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, 0);
        }

        return region;
    }

    @Override
    public CityGroup updateCityGroup(CityGroup cityGroup) {
        String sql = "UPDATE city_group SET city_names=? WHERE name=?";
        int rows = this.jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setArray(1, conn.createArrayOf("VARCHAR", cityGroup.getCities().toArray()));
            ps.setString(2, cityGroup.getName());
            return ps;
        });
        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, 0);
        }

        return cityGroup;
    }

    @Override
    public CityGroup getCityGroup(String name) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", name);

        return this.namedParameterJdbcTemplate.query(
                "SELECT city_names FROM city_group WHERE name=:name",
                parameters,
                rs -> {
                    if (rs.next()) {
                        Array array = rs.getArray("city_names");
                        String[] cityNames = (String[]) array.getArray();

                        CityGroup g = new CityGroup();
                        g.setName(name);
                        g.setCities(Arrays.asList(cityNames));

                        return g;
                    }
                    return null;
                });
    }

    @Override
    public void deleteRegion(String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", region);

        this.namedParameterJdbcTemplate.update("DELETE FROM region WHERE name=:region",
                parameters);
    }

    private Set<String> listCityNames(Collection<String> groupNames) {
        Set<String> cities = new TreeSet<>();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("names", groupNames);

        this.namedParameterJdbcTemplate.query(
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
        return this.namedParameterJdbcTemplate
                .queryForObject("SELECT gateway_region()",
                        Collections.emptyMap(),
                        String.class);
    }

    @Override
    public boolean doesAccountPlanExist() {
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM account LIMIT 1)",
                Collections.emptyMap(),
                Boolean.class);
    }
}
