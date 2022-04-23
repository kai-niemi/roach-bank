package io.roach.bank.web.api;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.LinkRelations;
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
                .regions())
                .withRel(LinkRelations.REGIONS_REL)
                .withTitle("All region names"));

        index.add(linkTo(methodOn(getClass())
                .cities())
                .withRel(LinkRelations.CITIES_REL)
                .withTitle("Currencies by city or region"));

        index.add(linkTo(methodOn(getClass())
                .currencies())
                .withRel(LinkRelations.CURRENCIES_REL)
                .withTitle("Currencies by city or region"));

        index.add(linkTo(methodOn(getClass())
                .regionCities(Collections.emptyList()))
                .withRel(LinkRelations.REGION_CITIES_REL)
                .withTitle("Cities grouped by region if available"));

        index.add(linkTo(methodOn(getClass())
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Gateway region"));

        return index;
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public Set<String> regions() {
        return metadataRepository.getRegions();
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public Map<String, Currency> cities() {
        return metadataRepository.getCities();
    }

    @GetMapping(value = "/currencies")
    @TransactionBoundary(readOnly = true)
    public Set<Currency> currencies() {
        return metadataRepository.getCurrencies();
    }

    @GetMapping(value = "/region-cities")
    @TransactionBoundary(readOnly = true)
    public Set<String> regionCities(@RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        return metadataRepository.getRegionCities(regions);
    }

    @GetMapping(value = "/gateway-region")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<String> gatewayRegion() {
        return ResponseEntity.ok(metadataRepository.getGatewayRegion());
    }
}
