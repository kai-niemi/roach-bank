package io.roach.bank.client.command;

import io.roach.bank.client.command.support.BankClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.*;
import org.springframework.util.StringUtils;

import static io.roach.bank.api.LinkRelations.*;

@ShellComponent
@ShellCommandGroup(Constants.CONFIG_COMMANDS)
public class Config extends AbstractCommand {
    @Autowired
    private BankClient bankClient;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region", "gr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.successf("%s", bankClient.getGatewayRegion());
    }

    @ShellMethod(value = "List region", key = {"list-regions", "lr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        bankClient.getRegions().forEach(r -> {
            console.successf("%s - groups: %s cities: %s", r.getName(), r.getCityGroups(), r.getCities());
        });
    }

    @ShellMethod(value = "List region cities", key = {"list-cities", "lc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "",
                    valueProvider = RegionProvider.class) String regions) {
        bankClient.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.successf("%s", s);
        });
    }

    @ShellMethod(value = "Configure table localities for multi-region", key = {"multi-region", "mr"})
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

//    http://localhost:8090/api/config/multiregion/configure
}
