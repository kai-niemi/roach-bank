package io.roach.bank.web.api;

import java.util.Collections;
import java.util.HashSet;
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
                .regionCities())
                .withRel(LinkRelations.REGIONS_REL)
                .withTitle("All regions and cities"));

        index.add(linkTo(methodOn(getClass())
                .regionCities(Collections.emptyList()))
                .withRel(LinkRelations.REGION_CITIES_REL)
                .withTitle("Cities filtered by gateway region"));
        index.add(linkTo(methodOn(getClass())
                .regionCities(null))
                .withRel(LinkRelations.REGION_CITIES_REL)
                .withTitle("Cities filtered by gateway region"));

        index.add(linkTo(methodOn(getClass())
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Gateway region"));

        return index;
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public Map<String, Set<String>> regionCities() {
        return metadataRepository.getRegionCities();
    }

    @GetMapping(value = "/region-cities")
    @TransactionBoundary(readOnly = true)
    public Set<String> regionCities(
            @RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        return metadataRepository.getRegionCities(regions);
    }

    @GetMapping(value = "/gateway-region")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<MessageModel> gatewayRegion() {
        return ResponseEntity.ok(new MessageModel(metadataRepository.getGatewayRegion()));
    }
}
