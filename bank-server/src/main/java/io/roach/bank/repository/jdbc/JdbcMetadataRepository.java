package io.roach.bank.repository.jdbc;

import java.util.*;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.roach.bank.annotation.TransactionControlService;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.web.api.MetadataException;

@Repository
@TransactionControlService
public class JdbcMetadataRepository implements MetadataRepository {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${roachbank.locality}")
    private String locality;

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
        return this.jdbcTemplate.queryForObject(
                "SELECT DISTINCT currency FROM region_config WHERE name=?",
                (rs, rowNum) -> Currency.getInstance(rs.getString(1)),
                region);
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
    public Map<String, Currency> resolveRegions(List<String> names) {
        if (names.isEmpty()) {
            names = getLocalRegions();
        }

        Map<String, Currency> result = new HashMap<>();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("names", names);

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

        if (result.isEmpty()) {
            throw new MetadataException("No regions found matching: " + names);
        }

        return result;
    }

    @Override
    public List<String> getLocalRegions() {
        List<String> listValues;

        if ("".equals(locality) || "all".equals(locality)) {
            listValues = this.jdbcTemplate.query(
                    "SELECT distinct(name),list_value FROM crdb_internal.partitions where name <> 'default'",
                    (resultSet, i) -> resultSet.getString(2));
        } else {
            listValues = this.jdbcTemplate.query(
                    "SELECT list_value FROM crdb_internal.partitions WHERE name=?",
                    (rs, rowNum) -> rs.getString(1),
                    locality);
        }

        Set<String> filteredValues = new HashSet<>();

        listValues.forEach(s -> {
            List<String> p = Arrays.stream(
                    s.split(",")).map(x -> x.trim().replaceAll("^\\('|'\\)$", ""))
                    .collect(Collectors.toList());
            filteredValues.addAll(p);
        });

        if (filteredValues.isEmpty()) {
            filteredValues.addAll(getRegions());
        }

        return new ArrayList<>(filteredValues);
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
}
