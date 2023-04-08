package io.roach.bank.web;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.Region;
import io.roach.bank.repository.MetadataRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/metadata")
public class MetadataController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private RegionResourceAssembler regionResourceAssembler;

    @Autowired
    private CityGroupAssembler cityGroupAssembler;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Region and city metadata");

        index.add(linkTo(methodOn(getClass())
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Local gateway region"));

        index.add(linkTo(methodOn(getClass())
                .listRegions())
                .withRel(LinkRelations.REGION_LIST_REL)
                .withTitle("List all regions"));

        index.add(linkTo(methodOn(getClass())
                .listCities(null))
                .withRel(LinkRelations.REGION_CITY_LIST_REL)
                .withTitle("List cities grouped by region(s)"));

        index.add(linkTo(methodOn(getClass())
                .listCityGroups())
                .withRel(LinkRelations.CITY_GROUP_LIST_REL)
                .withTitle("List all city groups"));

        return ResponseEntity.ok().body(index);
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<Region>> listRegions() {
        return CollectionModel.of(regionResourceAssembler.toCollectionModel(metadataRepository.listRegions()));
    }

    @GetMapping(value = "/regions/{region}")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<EntityModel<Region>> getRegion(@PathVariable("region") String region) {
        return ResponseEntity.ok(
                regionResourceAssembler.toModel(metadataRepository.getRegionByName(region)));
    }

    @PutMapping(value = "/regions")
    @TransactionBoundary
    public ResponseEntity<EntityModel<Region>> updateRegion(@RequestBody Region form) {
        return ResponseEntity.ok(regionResourceAssembler.toModel(
                metadataRepository.updateRegion(form)));
    }

    @DeleteMapping(value = "/regions")
    @TransactionBoundary
    public ResponseEntity<?> deleteRegion(@RequestBody Region form) {
        metadataRepository.deleteRegion(form.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/regions")
    @TransactionBoundary
    public ResponseEntity<EntityModel<Region>> addRegion(@Valid @RequestBody Region form) {
        Region region = metadataRepository.createRegion(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(regionResourceAssembler.toModel(region));
    }

    @GetMapping(value = "/city-groups")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<CityGroup>> listCityGroups() {
        return CollectionModel.of(cityGroupAssembler.toCollectionModel(metadataRepository.listCityGroups()));
    }

    @PutMapping(value = "/city-groups")
    @TransactionBoundary
    public ResponseEntity<EntityModel<CityGroup>> updateCityGroup(@RequestBody CityGroup cityGroup) {
        return ResponseEntity.ok(cityGroupAssembler.toModel(metadataRepository.updateCityGroup(cityGroup)));
    }

    @GetMapping(value = "/city-groups/{name}")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<EntityModel<CityGroup>> getCityGroup(@PathVariable("name") String name) {
        return ResponseEntity.ok(
                cityGroupAssembler.toModel(metadataRepository.getCityGroup(name)));
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<String> listCities(
            @RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        return CollectionModel.of(metadataRepository.listCities(regions));
    }

    @GetMapping(value = "/gateway-region")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<Map<String, String>> gatewayRegion() {
        return ResponseEntity.ok(Collections.singletonMap("region", metadataRepository.getGatewayRegion()));
    }

}
