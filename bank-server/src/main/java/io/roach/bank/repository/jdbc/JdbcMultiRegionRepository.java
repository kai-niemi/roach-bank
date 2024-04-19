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
import org.springframework.util.Assert;

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
        logger.info("Add regions {}", regions);

        Region primary = regions.stream()
                .filter(Region::isPrimary)
                .findFirst().orElseThrow(() -> new IllegalStateException("No primary region defined!"));

        jdbcTemplate.update("ALTER DATABASE roach_bank PRIMARY REGION \"" + primary.getDatabaseRegion() + "\"");

        regions.stream()
                .filter(region -> region.getDatabaseRegion() != null)
                .forEach(region -> {
                    if (!region.isPrimary() && !region.isSecondary()) {
                        jdbcTemplate.update("ALTER DATABASE roach_bank ADD REGION IF NOT EXISTS \""
                                + region.getDatabaseRegion() + "\"");
                    }
                });

        regions.stream()
                .filter(Region::isSecondary)
                .findFirst()
                .ifPresent(region ->
                        jdbcTemplate.update("ALTER DATABASE roach_bank SET SECONDARY REGION \""
                                + region.getDatabaseRegion() + "\""));
    }

    @Override
    public void dropDatabaseRegions(List<Region> regions) {
        logger.info("Drop regions {}", regions);

        regions.forEach(region -> {
            if (region.isPrimary()) {
                setPrimaryRegion(region);
            }
        });

        regions.stream()
                .filter(region -> region.getDatabaseRegion() != null)
                .forEach(region -> {
                    if (!region.isPrimary()) {
                        jdbcTemplate.update(
                                "ALTER DATABASE roach_bank DROP REGION IF EXISTS \"" + region.getDatabaseRegion()
                                        + "\"");
                    }
                });
    }

    @Override
    public void setPrimaryRegion(Region region) {
        logger.info("Set primary region {}", region.getDatabaseRegion());
        Assert.notNull(region.getDatabaseRegion(), "Database region is null (no mapping?)");
        jdbcTemplate.update("ALTER DATABASE roach_bank SET PRIMARY REGION \"" + region.getDatabaseRegion() + "\"");
    }

    @Override
    public void setSecondaryRegion(Region region) {
        logger.info("Set secondary region {}", region.getDatabaseRegion());
        Assert.notNull(region.getDatabaseRegion(), "Database region is null (no mapping?)");
        jdbcTemplate.update("ALTER DATABASE roach_bank SET SECONDARY REGION \"" + region.getDatabaseRegion() + "\"");
    }

    @Override
    public void dropSecondaryRegion() {
        logger.info("Drop secondary region");
        jdbcTemplate.update("ALTER DATABASE roach_bank DROP SECONDARY REGION");
    }

    @Override
    public void setSurvivalGoal(SurvivalGoal survivalGoal) {
        logger.info("Set survival goal to %s".formatted(survivalGoal));

        if (survivalGoal.equals(SurvivalGoal.REGION)) {
            jdbcTemplate.update("ALTER DATABASE roach_bank SURVIVE REGION FAILURE");
        } else {
            jdbcTemplate.update("ALTER DATABASE roach_bank SURVIVE ZONE FAILURE");
        }
    }

    @Override
    public void setGlobalTable(String table) {
        logger.info("Set table locality GLOBAL for %s".formatted(table));
        jdbcTemplate.update("ALTER TABLE " + table + " SET locality GLOBAL");
    }

    @Override
    public void setRegionalByTable(String table) {
        logger.info("Set table locality RBT for %s".formatted(table));
        jdbcTemplate.update("ALTER TABLE " + table + " SET locality REGIONAL BY TABLE IN PRIMARY REGION");
    }

    @Override
    public void setRegionalByRowTable(String table) {
        logger.info("Set table locality RBR for %s".formatted(table));

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ")
                .append(table)
                .append(" ADD COLUMN IF NOT EXISTS region crdb_internal_region AS (CASE");

        Deque<Region> primary = new ArrayDeque<>();

        metadataRepository.listRegions(List.of())
                .stream()
                .filter(region -> !region.getCities().isEmpty())
                .forEach(region -> {
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
                    sb.append(") THEN '").append(region.getDatabaseRegion()).append("'");
                });

        sb.append(" ELSE '")
                .append(primary.pop().getDatabaseRegion())
                .append("' END) STORED NOT NULL");

        logger.info("SQL: %s".formatted(sb.toString()));

        jdbcTemplate.execute(sb.toString());
        jdbcTemplate.execute("ALTER TABLE " + table + " SET LOCALITY REGIONAL BY ROW AS region");
    }
}
