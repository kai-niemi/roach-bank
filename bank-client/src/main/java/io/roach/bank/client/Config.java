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
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region","gr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.yellow("Gateway region is '%s'\n", restCommands.getGatewayRegion());
    }

    @ShellMethod(value = "List regions", key = {"list-regions","lr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        console.cyan("Available regions:\n");
        restCommands.getRegions().forEach((k, v) -> {
            console.yellow("%s: %s\n", k, v);
        });
        printGatewayRegion();
    }

    @ShellMethod(value = "List region cities", key = {"list-cities","lc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "") String regions) {
        console.cyan("-- region cities --\n");
        restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.yellow("%s\n", s);
        });
    }
}
