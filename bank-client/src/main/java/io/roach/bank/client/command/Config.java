package io.roach.bank.client.command;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import io.roach.bank.client.command.support.BankClient;

import static io.roach.bank.api.LinkRelations.CONFIG_INDEX_REL;
import static io.roach.bank.api.LinkRelations.CONFIG_MULTI_REGION_REL;
import static io.roach.bank.api.LinkRelations.withCurie;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Config extends AbstractCommand {
    @Autowired
    private BankClient bankClient;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region", "gr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.info("%s", bankClient.getGatewayRegion());
    }

    @ShellMethod(value = "List region", key = {"list-regions", "lr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        bankClient.getRegions().forEach(r -> {
            console.info("""
                                 Region: %s
                            City groups: %s
                                 Cities: %s
                            """,
                    r.getName(), r.getCityGroups(), r.getCities());
        });
    }

    @ShellMethod(value = "List region cities", key = {"list-cities", "lc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region name (gateway region if omitted)", defaultValue = "",
                    valueProvider = RegionProvider.class) String region) {
        console.textf(AnsiColor.BRIGHT_CYAN, "Region cities for region '%s'", region);
        bankClient.getRegionCities(Collections.singleton(region)).forEach(s -> console.info("%s", s));
    }

    @ShellMethod(value = "Configure table localities for multi-region",
            key = {"multi-region", "mr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void configureMultiRegion() {
        final Link submitLink = bankClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("configure-multiregion"))
                .asLink();

        ResponseEntity<String> response = bankClient.post(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Set primary database region", key = {"primary-region", "pr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void primaryRegion(@ShellOption(help = "region name",
            valueProvider = RegionProvider.class) String region) {
        final Link submitLink = bankClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("primary-region"))
                .withTemplateParameters(Collections.singletonMap("region", region))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Set primary region to '%s'", region);

        ResponseEntity<String> response = bankClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Set secondary database region", key = {"secondary-region", "sr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void secondaryRegion(@ShellOption(help = "region name",
            valueProvider = RegionProvider.class) String region) {
        final Link submitLink = bankClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("secondary-region"))
                .withTemplateParameters(Collections.singletonMap("region", region))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Set secondary region to '%s'", region);

        ResponseEntity<String> response = bankClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Add database regions", key = {"add-regions", "ar"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void addRegions() {
        final Link submitLink = bankClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("add-regions"))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Add database regions");

        ResponseEntity<String> response = bankClient.put(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }

    @ShellMethod(value = "Drop database regions", key = {"drop-regions", "dr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void dropRegions() {
        final Link submitLink = bankClient.fromRoot()
                .follow(withCurie(CONFIG_INDEX_REL))
                .follow(withCurie(CONFIG_MULTI_REGION_REL))
                .follow(withCurie("drop-regions"))
                .asLink();

        console.textf(AnsiColor.BRIGHT_CYAN, "Drop database regions");

        ResponseEntity<String> response = bankClient.delete(submitLink, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Unexpected HTTP status: {}", response);
        }

        logger.info("{}", response);
    }
}
