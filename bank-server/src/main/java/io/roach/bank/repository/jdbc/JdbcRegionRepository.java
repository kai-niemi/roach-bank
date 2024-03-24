package io.roach.bank.repository.jdbc;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.Region;
import io.roach.bank.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class JdbcRegionRepository implements RegionRepository {
    @Autowired
    private Environment environment;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<Region> listRegions(Collection<String> regions) {
        if (regions.isEmpty()) {
            return this.namedParameterJdbcTemplate.query(
                    "SELECT name,city_names FROM region",
                    Map.of(),
                    regionRowMapper());
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("regions", regions);
        return this.namedParameterJdbcTemplate.query(
                "SELECT name,city_names FROM region WHERE name in (:regions)",
                parameters,
                regionRowMapper());
    }

    @Override
    public Set<String> listCities(Collection<Region> regions) {
        return regions.stream()
                .flatMap(region -> region.getCities().stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Region getRegionByName(String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", region);
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT name,city_names FROM region WHERE name = :name",
                parameters,
                regionRowMapper());
    }

    @Override
    public String getGatewayRegion() {
        if (ProfileNames.acceptsPostgresSQL(environment)) {
            return listRegions(List.of()).get(0).getName();
        }

        String gateway = this.namedParameterJdbcTemplate
                .queryForObject("SELECT gateway_region()",
                        Collections.emptyMap(),
                        String.class);

        return mapDatabaseRegionToBankRegion(gateway);
    }

    @Override
    public boolean hasAccountPlan() {
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM account LIMIT 1)",
                Collections.emptyMap(),
                Boolean.class);
    }

    private RowMapper<Region> regionRowMapper() {
        return (rs, rowNum) -> {
            Region region = new Region();
            region.setName(rs.getString("name"));
            region.setCities(Arrays.asList((String[]) rs.getArray("city_names").getArray()));

            String databaseRegion = mapBankRegionToDatabaseRegion(region.getName());
            region.setDatabaseRegion(databaseRegion);
            region.setPrimary(isPrimary(databaseRegion));

            return region;
        };
    }

    private String mapDatabaseRegionToBankRegion(String databaseRegion) {
        // Look for mapping against bank regions
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", databaseRegion);

        try {
            return this.namedParameterJdbcTemplate.queryForObject(
                    "SELECT region from region_mapping WHERE crdb_region = :region",
                    parameters,
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return databaseRegion;
        }
    }

    private String mapBankRegionToDatabaseRegion(String bankRegion) {
        // Look for mapping against bank regions
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", bankRegion);
        try {
            return this.namedParameterJdbcTemplate.queryForObject(
                    "SELECT region FROM [SHOW regions] "
                            + "WHERE region in (SELECT crdb_region from region_mapping rm WHERE rm.region = :region)",
                    parameters,
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private boolean isPrimary(String databaseRegion) {
        if (databaseRegion == null) {
            return false;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("region", databaseRegion);
        try {
            //noinspection DataFlowIssue
            return this.namedParameterJdbcTemplate.queryForObject(
                    "SELECT (primary_region_of[1]='roach_bank') as primary, region FROM [SHOW regions] WHERE region = :region",
                    parameters,
                    (rs, rowNum) -> rs.getBoolean(1));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
