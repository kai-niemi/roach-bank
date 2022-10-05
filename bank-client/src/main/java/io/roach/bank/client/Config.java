package io.roach.bank.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.client.support.RestCommands;

@ShellComponent
@ShellCommandGroup(Constants.METADATA_COMMANDS)
public class Config extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.yellow("%s\n", restCommands.getGatewayRegion());
    }

    @ShellMethod(value = "List regions", key = {"list-regions"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        console.cyan("Available regions:\n");
        restCommands.getRegions().forEach((k, v) -> {
            console.yellow("%s: %s\n", k, v);
        });
        printGatewayRegion();
    }

    @ShellMethod(value = "List cities", key = {"list-cities"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "") String regions) {
        console.yellow("Gateway: %s\n", restCommands.getGatewayRegion());
        console.cyan("-- region '%s' cities --\n", regions);
        restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.yellow("%s\n", s);
        });
    }
}
