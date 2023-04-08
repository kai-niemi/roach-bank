package io.roach.bank.client.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import io.roach.bank.client.command.support.RestCommands;

@ShellComponent
@ShellCommandGroup(Constants.METADATA_COMMANDS)
public class Config extends AbstractCommand {
    @Autowired
    private RestCommands restCommands;

    @ShellMethod(value = "Print gateway region", key = {"gateway-region","gr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void printGatewayRegion() {
        console.successf("%s", restCommands.getGatewayRegion());
    }

    @ShellMethod(value = "List region", key = {"list-regions", "lr"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegions() {
        restCommands.getRegions().forEach(r -> {
            console.successf("%s - %s", r.getName(), r.getCities());
        });
    }
    @ShellMethod(value = "List region cities", key = {"list-cities","lc"})
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void listRegionCities(
            @ShellOption(help = "region names (gateway region if omitted)", defaultValue = "",
                    valueProvider = RegionProvider.class) String regions) {
        restCommands.getRegionCities(StringUtils.commaDelimitedListToSet(regions)).forEach(s -> {
            console.successf("%s", s);
        });
    }
}
