package io.roach.bank.web;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.domain.Region;
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

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping(value = "/api/metadata")
public class MetadataController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private RegionResourceAssembler regionResourceAssembler;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Region and city metadata");

        index.add(linkTo(methodOn(getClass())
                .listRegions())
                .withRel(LinkRelations.REGION_LIST_REL)
                .withTitle("All region names"));

        index.add(linkTo(methodOn(getClass())
                .listCities(null))
                .withRel(LinkRelations.REGION_CITIES_REL)
                .withTitle("Cities grouped by region(s)"));

        index.add(linkTo(methodOn(getClass())
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Gateway region"));

        index.add(linkTo(methodOn(getClass())
                .listDatabaseRegions())
                .withRel(LinkRelations.DATABASE_REGIONS_REL)
                .withTitle("Database regions"));

        return ResponseEntity.ok().body(index);
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<Region>> listRegions() {
        return CollectionModel.of(regionResourceAssembler.toCollectionModel(metadataRepository.listRegions())
                .add(linkTo(methodOn(getClass())
                        .listRegions())
                        .withSelfRel()
                        .andAffordance(afford(methodOn(getClass()).addRegion(null)))));
    }


    @PostMapping(value = "/regions")
    @TransactionBoundary
    public ResponseEntity<EntityModel<Region>> addRegion(@Valid @RequestBody Region form) {
        Region region = metadataRepository.addRegion(form.getProvider(), form.getName(), form.getCityGroups());
        return ResponseEntity.status(HttpStatus.CREATED).body(regionResourceAssembler.toModel(region));
    }

    @GetMapping(value = "/regions/{provider}/{region}")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<EntityModel<Region>> getRegion(@PathVariable("provider") String provider,
                                                         @PathVariable("region") String region) {
        return ResponseEntity.ok(regionResourceAssembler.toModel(metadataRepository.getRegion(provider, region)));
    }

    @DeleteMapping(value = "/regions/{provider}/{region}")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<String> deleteRegion(
            @PathVariable("provider") String provider,
            @PathVariable("region") String region) {
        metadataRepository.deleteRegion(provider, region);
        return ResponseEntity.ok("Deleted " + region);
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<String> listCities(
            @RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        return CollectionModel.of(metadataRepository.listCities(regions));
    }

    @GetMapping(value = "/gateway-region")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<String> gatewayRegion() {
        return ResponseEntity.ok(metadataRepository.getGatewayRegion());
    }

    @GetMapping(value = "/database-regions")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<Region> listDatabaseRegions() {
        return CollectionModel.of(metadataRepository.listDatabaseRegions())
                .add(linkTo(methodOn(getClass()).listDatabaseRegions())
                        .withSelfRel());
    }
}
