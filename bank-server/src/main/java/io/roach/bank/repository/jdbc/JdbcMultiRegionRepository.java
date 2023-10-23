package io.roach.bank.repository.jdbc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.roach.bank.api.Region;
import io.roach.bank.domain.SurvivalGoal;
import io.roach.bank.repository.MultiRegionRepository;
import io.roach.bank.repository.RegionRepository;

@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class JdbcMultiRegionRepository implements MultiRegionRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RegionRepository metadataRepository;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void addDatabaseRegions(List<Region> regions) {
        if (regions.size() < 3) {
            logger.warn("Expected at least 3 regions - found {}", regions.size());
        }

        regions.forEach(region -> {
            if (region.isPrimary()) {
                jdbcTemplate.update("ALTER DATABASE roach_bank PRIMARY REGION \"" + region.getName() + "\"");
            } else {
                jdbcTemplate.update("ALTER DATABASE roach_bank ADD REGION IF NOT EXISTS \"" + region.getName() + "\"");
            }
        });
    }

    @Override
    public void dropDatabaseRegions(List<Region> regions) {
        regions.forEach(region -> {
            if (region.isPrimary()) {
                setPrimaryRegion(region);
            }
        });
        regions.forEach(region -> {
            if (!region.isPrimary()) {
                jdbcTemplate.update("ALTER DATABASE roach_bank DROP REGION IF EXISTS \"" + region.getName() + "\"");
            }
        });
    }

    @Override
    public void setPrimaryRegion(Region region) {
        jdbcTemplate.update("ALTER DATABASE roach_bank SET PRIMARY REGION \"" + region.getName() + "\"");
    }

    @Override
    public void setSecondaryRegion(Region region) {
        jdbcTemplate.update("ALTER DATABASE roach_bank SET SECONDARY REGION \"" + region.getName() + "\"");
    }

    @Override
    public void dropSecondaryRegion() {
        jdbcTemplate.update("ALTER DATABASE roach_bank DROP SECONDARY REGION");
    }

    @Override
    public void setSurvivalGoal(SurvivalGoal survivalGoal) {
        if (survivalGoal.equals(SurvivalGoal.REGION)) {
            jdbcTemplate.update("ALTER DATABASE roach_bank SURVIVE REGION FAILURE");
        } else {
            jdbcTemplate.update("ALTER DATABASE roach_bank SURVIVE ZONE FAILURE");
        }
    }

    @Override
    public void addGloalTable(String table) {
        jdbcTemplate.update("ALTER TABLE " + table + " SET locality GLOBAL");
    }

    @Override
    public void addRegionalByRowTable(String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(table)
                .append(" ADD COLUMN IF NOT EXISTS region crdb_internal_region AS (CASE");

        Deque<Region> primary = new ArrayDeque<>();

        metadataRepository.listRegions().forEach(region -> {
            if (region.isPrimary()) {
                primary.push(region);
            }
            sb.append(" WHEN city IN (");
            boolean sep = false;
            for (String city : region.getCities()) {
                if (sep) {
                    sb.append(",");
                }
                sep = true;
                sb.append("'").append(city).append("'");
            }
            sb.append(") THEN '").append(region.getName()).append("'");
        });

        sb.append(" ELSE '")
                .append(primary.pop().getName())
                .append("' END) STORED NOT NULL");

        jdbcTemplate.execute(sb.toString());

        jdbcTemplate.execute("ALTER TABLE " + table + " SET LOCALITY REGIONAL BY ROW AS region");
    }

    @Override
    public void addRegionalTable(String table) {
        jdbcTemplate.update("ALTER TABLE " + table + " SET LOCALITY REGIONAL");
    }
}
