package io.roach.bank.repository.jdbc;

import io.roach.bank.api.Region;
import io.roach.bank.repository.RegionRepository;
import io.roach.bank.util.MetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class JdbcRegionRepository implements RegionRepository {
    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Region> listRegions(Collection<String> regions) {
        if (regions.isEmpty()) {
            return this.namedParameterJdbcTemplate.query(
                    "SELECT * FROM region",
                    Map.of(),
                    regionRowMapper());
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("regions", regions);
        return this.namedParameterJdbcTemplate.query(
                "SELECT * FROM region WHERE name in (:regions)",
                parameters,
                regionRowMapper());
    }

    @Override
    public Set<String> listCities(Collection<Region> regions) {
        return regions.stream()
                .flatMap(region -> region.getCities().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Region getRegionByName(String region) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", region);
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM region WHERE name = :name",
                parameters,
                regionRowMapper());
    }

    @Override
    public String getGatewayRegion() {
        if (!MetadataUtils.isCockroachDB(dataSource)) {
            return listRegions(List.of()).get(0).getName();
        }

        String gateway = this.namedParameterJdbcTemplate
                .queryForObject("SELECT gateway_region()",
                        Collections.emptyMap(),
                        String.class);

        return mapDatabaseRegionToBankRegion(gateway);
    }

    @Override
    public Optional<String> getPrimaryRegion() {
        if (MetadataUtils.isCockroachDB(dataSource)) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            try {
                return Optional.ofNullable(
                        mapDatabaseRegionToBankRegion(
                                this.namedParameterJdbcTemplate.queryForObject(
                                        "select region from [SHOW REGIONS FROM DATABASE roach_bank] WHERE \"primary\" = true",
                                        parameters,
                                        String.class)));
            } catch (EmptyResultDataAccessException e) {
                // ok
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getSecondaryRegion() {
        if (MetadataUtils.isCockroachDB(dataSource)) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            try {
                return Optional.ofNullable(
                        mapDatabaseRegionToBankRegion(this.namedParameterJdbcTemplate.queryForObject(
                                "select region from [SHOW REGIONS FROM DATABASE roach_bank] WHERE \"secondary\" = true",
                                parameters,
                                String.class)));
            } catch (EmptyResultDataAccessException e) {
                // ok
            }
        }
        return Optional.empty();
    }

    @Override
    public Boolean hasExistingAccountPlan() {
        return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM account LIMIT 1)",
                Collections.emptyMap(),
                Boolean.class);
    }

    @Override
    public void createRegion(Region region) {
        jdbcTemplate.update("INSERT INTO region (name,city_names,is_primary,is_secondary) VALUES (?,?,?,?)",
                ps -> {
                    Connection conn = ps.getConnection();
                    ps.setString(1, region.getName());
                    ps.setArray(2, conn.createArrayOf("VARCHAR", region.getCities().toArray(new String[0])));
                    ps.setBoolean(3, region.isPrimary());
                    ps.setBoolean(4, region.isSecondary());
                });
    }

    @Override
    public void createRegionMappings(Map<String, String> mappings) {
        mappings.forEach((k, v) -> {
            jdbcTemplate.update("INSERT INTO region_mapping (crdb_region,region) VALUES (?,?)",
                    ps -> {
                        ps.setString(1, k);
                        ps.setString(2, v);
                    });
        });
    }

    private RowMapper<Region> regionRowMapper() {
        return (rs, rowNum) -> {
            Region region = new Region();
            region.setName(rs.getString("name"));
            region.setCities(new TreeSet<>(
                    Arrays.asList((String[]) rs.getArray("city_names").getArray())));
            region.setPrimary(rs.getBoolean("is_primary"));
            region.setSecondary(rs.getBoolean("is_secondary"));
            region.setDatabaseRegion(mapBankRegionToDatabaseRegion(region.getName()));
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
        if (!MetadataUtils.isCockroachDB(dataSource)) {
            return bankRegion;
        }
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
            return bankRegion;
        }
    }
}
