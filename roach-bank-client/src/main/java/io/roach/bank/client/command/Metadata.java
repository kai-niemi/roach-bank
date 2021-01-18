package io.roach.bank.client.command;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import io.roach.bank.api.BankLinkRelations;

import static io.roach.bank.api.BankLinkRelations.*;

@ShellComponent
@ShellCommandGroup(Constants.API_META_COMMANDS)
public class Metadata extends RestCommandSupport {
    @ShellMethod("Print ISO-4217 currency codes")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void currencyCodes() {
        ResponseEntity<List> entity = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(CURRENCIES_REL))
                .toEntity(List.class);
        entity.getBody().forEach(o -> console.info("%s", o));
    }

    @ShellMethod("Print all regions")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void regionNames() {
        ResponseEntity<List> entity = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(REGIONS_REL))
                .toEntity(List.class);
        entity.getBody().forEach(o -> console.info("%s", o));
    }

    @ShellMethod(value = "Print all region groups")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void regionGroups() {
        Map result = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(REGION_GROUPS_REL))
                .toObject(Map.class);
        result.forEach((k, v) -> console.info(" %s -> %s", k, v));
    }

    @ShellMethod("Print all region currencies")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void regionCurrencies() {
        Map result = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(REGION_CURRENCY_REL))
                .toObject(Map.class);
        result.forEach((k, v) -> console.info(" %s -> %s", k, v));
    }

    @ShellMethod("Print local region name")
    @ShellMethodAvailability(Constants.CONNECTED_CHECK)
    public void regionName() {
        ResponseEntity<String> entity = traverson.fromRoot()
                .follow(BankLinkRelations.withCurie(META_REL))
                .follow(BankLinkRelations.withCurie(REGION_NAME_REL))
                .toEntity(String.class);
        console.info("%s", entity.getBody());
    }
}
