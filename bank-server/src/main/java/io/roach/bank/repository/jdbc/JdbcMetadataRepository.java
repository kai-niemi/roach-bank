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

import io.roach.bank.repository.MetadataRepository;

@Repository
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
    public List<String> getCities() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT city FROM region_map group by city",
                (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public List<Currency> getCurrencies() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT currency FROM region_map group by currency",
                (rs, rowNum) -> Currency.getInstance(rs.getString(1)));
    }

    @Override
    public List<String> getRegions() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT region FROM region_map group by region",
                (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public Currency getCityCurrency(String city) {
        return this.jdbcTemplate.queryForObject(
                "SELECT DISTINCT currency FROM region_map WHERE city=?",
                (rs, rowNum) -> Currency.getInstance(rs.getString(1)),
                city);
    }

    @Override
    public Map<String, List<String>> getRegionCities() {
        Map<String, List<String>> result = new HashMap<>();
        getRegions().forEach(region -> result.put(region, citiesByRegion(region)));
        return result;
    }

    @Override
    public Map<Currency, List<String>> getCurrencyCities() {
        Map<Currency, List<String>> result = new HashMap<>();
        getCurrencies().forEach(currency -> result.put(currency, citiesByCurrency(currency)));
        return result;
    }

    @Override
    public Map<String, Currency> getCityCurrency(Collection<String> cities) {
        if (cities.isEmpty()) {
            cities = getCities();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("cities", cities);

        Map<String, Currency> result = new HashMap<>();

        this.namedParameterJdbcTemplate.query(
                "SELECT city,currency FROM region_map "
                        + "WHERE city IN (:cities) "
                        + "GROUP BY city,currency",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), Currency.getInstance(rs.getString(2)));
                    return null;
                });

        if (!result.isEmpty()) {
            return result;
        }

        return getCityCurrenciesByRegion(cities);
    }

    private Map<String, Currency> getCityCurrenciesByRegion(Collection<String> regions) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("regions", regions);
        Map<String, Currency> result = new HashMap<>();
        this.namedParameterJdbcTemplate.query(
                "SELECT city,currency FROM region_map "
                        + "WHERE region IN (:regions::region_code) "
                        + "GROUP BY city,currency",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), Currency.getInstance(rs.getString(2)));
                    return null;
                });
        return result;
    }

    @Override
    public List<String> getLocalCities() {
        List<String> cities = new ArrayList<>();

        if (partitionTableExists()) {
            Set<String> regions = new HashSet<>();
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

            listValues.forEach(s -> {
                List<String> p = Arrays.stream(
                                s.split(",")).map(x -> x.trim().replaceAll("^\\('|'\\)$", ""))
                        .collect(Collectors.toList());
                regions.addAll(p);
            });

            regions.forEach(region -> cities.addAll(citiesByRegion(region)));
        }

        if (cities.isEmpty()) {
            cities.addAll(getCities());
        }

        return cities;
    }

    private boolean partitionTableExists() {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables "
                        + "WHERE table_schema='crdb_internal' and table_name = 'partitions' LIMIT 1",
                Integer.class) != 0;
    }

    private List<String> citiesByCurrency(Currency currency) {
        return this.jdbcTemplate.query(
                "SELECT city FROM region_map WHERE currency = ?::currency_code group by city",
                (rs, rowNum) -> rs.getString(1),
                currency.getCurrencyCode()
        );
    }

    private List<String> citiesByRegion(String region) {
        return this.jdbcTemplate.query(
                "SELECT city FROM region_map WHERE region = ?::region_code group by city",
                (rs, rowNum) -> rs.getString(1),
                region);
    }
}
