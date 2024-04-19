package io.roach.bank.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.TableModel;

import io.roach.bank.api.Region;
import io.roach.bank.client.support.HypermediaClient;
import io.roach.bank.client.support.TableUtils;
import io.roach.bank.domain.SurvivalGoal;

import static io.roach.bank.api.LinkRelations.CONFIG_INDEX_REL;
import static io.roach.bank.api.LinkRelations.CONFIG_MULTI_REGION_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Regions extends AbstractCommand {
    @Autowired
    private HypermediaClient hypermediaClient;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region", "gr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.info("%s", hypermediaClient.getGatewayRegion());
    }

    @ShellMethod(value = "List all regions", key = {"list-regions", "lr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        List<Region> regions = new ArrayList<>(hypermediaClient.getRegions());

        console.success(TableUtils.prettyPrint(new TableModel() {
            @Override
            public int getRowCount() {
                return regions.size() + 1;
            }

            @Override
            public int getColumnCount() {
                return 5;
            }

            @Override
            public Object getValue(int row, int column) {
                if (row == 0) {
                    return List.of("Name", "Cities", "Primary", "Secondary", "DB Region").get(column);
                }

                switch (column) {
                    case 0 -> {
                        return regions.get(row - 1).getName();
                    }
                    case 1 -> {
                        return regions.get(row - 1).getCities();
                    }
                    case 2 -> {
                        return regions.get(row - 1).isPrimary();
                    }
                    case 3 -> {
                        return regions.get(row - 1).isSecondary();
                    }
                    case 4 -> {
                        return regions.get(row - 1).getDatabaseRegion();
                    }
                    default -> {
                        return "??";
                    }
                }
            }
        }));
    }

    @ShellMethod(value = "List region cities", key = {"list-cities", "lc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region name (gateway region if omitted)", defaultValue = "",
                    valueProvider = RegionProvider.class) String region) {
        hypermediaClient.getRegionCities(region).forEach(s -> console.success("%s", s));
    }

    @ShellMethod(value = "Enable multi-region table localities",
            key = {"enable-multi-region", "emr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void enableMultiRegion() {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("enable-multiregion"))
                .asLink();

        ResponseEntity<String> response = hypermediaClient.post(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Disable multi-region table localities",
            key = {"disable-multi-region", "dmr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void disableMultiRegion() {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("disable-multiregion"))
                .asLink();

        ResponseEntity<String> response = hypermediaClient.post(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Set primary database region", key = {"primary-region", "pr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void primaryRegion(@ShellOption(help = "region name",
            valueProvider = RegionProvider.class) String region) {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("primary-region"))
                .withTemplateParameters(Collections.singletonMap("region", region))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Set primary region to '%s'", region);

        ResponseEntity<String> response = hypermediaClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Set secondary database region", key = {"secondary-region", "sr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void secondaryRegion(@ShellOption(help = "region name",
            valueProvider = RegionProvider.class) String region) {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("secondary-region"))
                .withTemplateParameters(Collections.singletonMap("region", region))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Set secondary region to '%s'", region);

        ResponseEntity<String> response = hypermediaClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Add database regions", key = {"add-regions", "ar"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void addRegions() {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("add-regions"))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Add database regions");

        ResponseEntity<String> response = hypermediaClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Drop database regions", key = {"drop-regions", "dr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void dropRegions() {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("drop-regions"))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Drop database regions");

        ResponseEntity<String> response = hypermediaClient.delete(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Set survival goal", key = {"survival-goal", "sg"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void survivalGoal(@ShellOption(help = "survival goal",
            valueProvider = EnumValueProvider.class) SurvivalGoal survivalGoal) {
        final Link submitLink = hypermediaClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("survival-goal"))
                .withTemplateParameters(Collections.singletonMap("goal", survivalGoal))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Set survival goal '%s'", survivalGoal);

        ResponseEntity<String> response = hypermediaClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

}
