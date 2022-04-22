package io.roach.bank.repository.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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


//    @Override
    public List<String> getCities() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT city FROM region_map group by city",
                (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public Set<Currency> getCurrencies() {
        return this.namedParameterJdbcTemplate.query(
                "SELECT currency FROM region_map group by currency",
                (rs, rowNum) -> Currency.getInstance(rs.getString(1)));
    }

    @Override
    public Set<String> getRegions() {
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
    public Map<String, List<String>> getRegionToCityMap() {
        Map<String, List<String>> result = new HashMap<>();
        getRegions().forEach(region -> result.put(region, citiesByRegions(Collections.singleton(region))));
        return result;
    }

    @Override
    public Map<Currency, List<String>> getCurrencyToCityMap() {
        Map<Currency, List<String>> result = new HashMap<>();
        getCurrencies().forEach(currency -> result.put(currency, citiesByCurrency(currency)));
        return result;
    }

    @Override
    public Set<String> getRegionCities(Collection<String> regions) {
        List<String> cities = new ArrayList<>();

        if (regions.isEmpty()) {
            if (StringUtils.hasLength(locality) && !"all".equals(locality)) {
                regions.add(locality);
            } else if ("all".equals(locality)) {
                regions.addAll(getRegions());
            }
        }

        cities.addAll(citiesByRegions(regions));

        return cities;
    }

    @Override
    public Map<String, Currency> getCityToCurrencyMap() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        Map<String, Currency> result = new HashMap<>();

        this.namedParameterJdbcTemplate.query(
                "SELECT city,currency FROM region_map "
                        + "GROUP BY city,currency",
                parameters,
                (rs, rowNum) -> {
                    result.put(rs.getString(1), Currency.getInstance(rs.getString(2)));
                    return null;
                });

            return result;
    }

    private List<String> citiesByCurrency(Currency currency) {
        return this.jdbcTemplate.query(
                "SELECT city FROM region_map WHERE currency = ?::currency_code group by city",
                (rs, rowNum) -> rs.getString(1),
                currency.getCurrencyCode()
        );
    }

    private List<String> citiesByRegions(Collection<String> regions) {
        return this.jdbcTemplate.query(
                "SELECT city FROM region_map WHERE region in (?::region_code) group by city",
                (rs, rowNum) -> rs.getString(1),
                regions);
    }
}
