package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.client.command.support.RestCommands;
import io.roach.bank.client.provider.RegionProvider;

@ShellComponent
@ShellCommandGroup(Constants.METADATA_COMMANDS)
public class Config extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.successf("%s", restCommands.getGatewayRegion());
    }

    @ShellMethod(value = "List regions", key = {"list-regions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        console.infof("Available regions and cities:\n");
        restCommands.getRegions().forEach((k, v) -> {
            console.successf("%s: %s", k, v);
        });
    }

    @ShellMethod(value = "List cities", key = {"list-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "",
                    valueProvider = RegionProvider.class) String regions) {
        console.infof("-- region '%s' cities --", regions);
        restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.successf("%s", s);
        });
    }
}
