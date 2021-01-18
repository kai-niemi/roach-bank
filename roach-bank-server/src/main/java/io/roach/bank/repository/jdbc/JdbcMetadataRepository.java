package io.roach.bank.repository.jdbc;

import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.roach.bank.annotation.TransactionMandatory;
import io.roach.bank.api.RegionConfig;
import io.roach.bank.repository.MetadataRepository;

@Repository
@TransactionMandatory
public class JdbcMetadataRepository implements MetadataRepository {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<String> getRegions() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT name FROM region_config group by name",
                (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public List<String> getGroups() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT group_name FROM region_config group by group_name",
                (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public List<Currency> getCurrencies() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT currency FROM region_config group by currency",
                (rs, rowNum) -> Currency.getInstance(rs.getString(1)));
    }

    @Override
    public Currency getRegionCurrency(String region) {
        try {
            return this.jdbcTemplate.queryForObject(
                    "SELECT DISTINCT currency FROM region_config WHERE name=?",
                    (rs, rowNum) -> Currency.getInstance(rs.getString(1)),
                    region);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getGroupRegions() {
        Map<String, List<String>> result = new HashMap<>();
        getGroups().forEach(group -> {
            result.put(group, regionsByGroup(group));
        });
        return result;
    }

    @Override
    public Map<Currency, List<String>> getCurrencyRegions() {
        Map<Currency, List<String>> result = new HashMap<>();
        getCurrencies().forEach(currency -> {
            result.put(currency, regionsByCurrency(currency));
        });
        return result;
    }

    @Override
    public Map<String, Currency> resolveRegions(List<String> regions) {
        Map<String, Currency> result = new HashMap<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("names", regions);
        this.namedParameterJdbcTemplate.query(
                "SELECT c.name,c.currency FROM region_config c "
                        + "INNER JOIN region_group g ON c.group_name=g.name "
                        + "WHERE g.name IN (:names) "
                        + "OR c.name IN (:names) "
                        + "GROUP BY c.name,c.currency",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), Currency.getInstance(rs.getString(2)));
                    return null;
                });
        return result;
    }

    @Override
    public List<RegionConfig> getRegionConfigs() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        return this.namedParameterJdbcTemplate.query(
                "SELECT name,currency FROM region_config GROUP BY name,currency",
                parameters,
                (rs, rowNum) -> {
                    RegionConfig regionConfig = new RegionConfig();
                    regionConfig.setRegion(rs.getString(1));
                    regionConfig.setCurrency(Currency.getInstance(rs.getString(2)));
                    regionConfig.setGroupNames(groupNamesByRegion(rs.getString(1)));
                    return regionConfig;
                });
    }

    private List<String> regionsByCurrency(Currency currency) {
        return this.jdbcTemplate.query(
                "SELECT name FROM region_config WHERE currency=? group by name",
                new Object[] {currency.getCurrencyCode()},
                (rs, rowNum) -> rs.getString(1));
    }

    private List<String> regionsByGroup(String group) {
        return this.jdbcTemplate.query(
                "SELECT name FROM region_config WHERE group_name=? group by name",
                new Object[] {group},
                (rs, rowNum) -> rs.getString(1));
    }

    private List<String> groupNamesByRegion(String name) {
        return this.jdbcTemplate.query(
                "SELECT group_name FROM region_config WHERE name=?",
                new Object[] {name},
                (rs, rowNum) -> rs.getString(1));
    }
}
