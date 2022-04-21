package io.roach.bank.web.api;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.web.support.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/metadata")
public class MetadataController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MetadataRepository metadataRepository;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass())
                .regionCities())
                .withRel(BankLinkRelations.REGION_CITIES_REL)
                .withTitle("All cities grouped by region"));

        index.add(linkTo(methodOn(getClass())
                .citiesCurrency(Collections.emptyList()))
                .withRel(BankLinkRelations.CITY_CURRENCY_REL)
                .withTitle("Currencies by city or region"));
        index.add(linkTo(methodOn(getClass())
                .citiesCurrency(null))
                .withRel(BankLinkRelations.CITY_CURRENCY_REL)
                .withTitle("Currencies by city or region"));

        index.add(linkTo(methodOn(getClass())
                .listCurrencies())
                .withRel(BankLinkRelations.CURRENCIES_REL)
                .withTitle("All currencies"));

        index.add(linkTo(methodOn(getClass())
                .listCities())
                .withRel(BankLinkRelations.CITIES_REL)
                .withTitle("All city names"));

        index.add(linkTo(methodOn(getClass())
                .listLocalCities())
                .withRel(BankLinkRelations.LOCAL_CITIES_REL)
                .withTitle("All local cities filtered by locality if available"));

        index.add(linkTo(methodOn(getClass())
                .listRegions())
                .withRel(BankLinkRelations.REGIONS_REL)
                .withTitle("All region names"));

        return index;
    }

    @GetMapping(value = "/region-cities")
    @TransactionBoundary(readOnly = true)
    public Map<String, List<String>> regionCities() {
        return metadataRepository.getRegionCities();
    }

    @GetMapping(value = "/currencies")
    @TransactionBoundary(readOnly = true)
    public List<Currency> listCurrencies() {
        return metadataRepository.getCurrencies();
    }

    @GetMapping(value = "/cities-local")
    @TransactionBoundary(readOnly = true)
    public List<String> listLocalCities() {
        return metadataRepository.getLocalCities();
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public List<String> listCities() {
        return metadataRepository.getCities();
    }

    @GetMapping(value = "/cities-currency")
    @TransactionBoundary(readOnly = true)
    public Map<String, Currency> citiesCurrency(
            @RequestParam(name = "cities", required = false, defaultValue = "") List<String> cities) {
        return metadataRepository.getCityCurrency(cities);
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public List<String> listRegions() {
        return metadataRepository.getRegions();
    }
}
