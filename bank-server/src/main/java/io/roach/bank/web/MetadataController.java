package io.roach.bank.web;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.CityGroup;
import io.roach.bank.api.Region;
import io.roach.bank.repository.MetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

//        index.add(linkTo(methodOn(getClass())
//                .syncRegions())
//                .withRel(LinkRelations.SYNC_REGIONS)
//                .withTitle("Synchronize database and bank regions"));

        index.add(linkTo(methodOn(getClass())
                .listRegions())
                .withRel(LinkRelations.REGION_LIST_REL)
                .withTitle("List all regions"));

        index.add(linkTo(methodOn(getClass())
                .listCityGroups())
                .withRel(LinkRelations.CITY_GROUP_LIST_REL)
                .withTitle("List all city groups"));

        index.add(linkTo(methodOn(getClass())
                .listCities(null))
                .withRel(LinkRelations.REGION_CITY_LIST_REL)
                .withTitle("List cities grouped by region(s)"));

        index.add(linkTo(methodOn(getClass())
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Get local gateway region"));

        index.add(linkTo(methodOn(getClass())
                .listDatabaseRegions())
                .withRel(LinkRelations.DATABASE_REGIONS_REL)
                .withTitle("List only database regions"));

        return ResponseEntity.ok().body(index);
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<Region>> listRegions() {
        return CollectionModel.of(regionResourceAssembler.toCollectionModel(metadataRepository.listRegions())
                .add(linkTo(methodOn(getClass())
                        .listRegions())
                        .withSelfRel()));
    }

    @GetMapping(value = "/city-groups")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<CityGroup> listCityGroups() {
        return CollectionModel.of(metadataRepository.listCityGroups())
                .add(linkTo(methodOn(getClass())
                        .listRegions())
                        .withSelfRel());
    }

    @GetMapping(value = "/regions/{region}")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<EntityModel<Region>> getRegion(@PathVariable("region") String region) {
        return ResponseEntity.ok(regionResourceAssembler.toModel(metadataRepository.getRegionByName(region)));
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<String> listCities(
            @RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        return CollectionModel.of(metadataRepository.listCities(regions));
    }

    @GetMapping(value = "/gateway-region")
    @TransactionBoundary(readOnly = true)
    public ResponseEntity<Map<String,String>> gatewayRegion() {
        return ResponseEntity.ok(Collections.singletonMap("region", metadataRepository.getGatewayRegion()));
    }

    @GetMapping(value = "/database-regions")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<Region> listDatabaseRegions() {
        return CollectionModel.of(metadataRepository.listDatabaseRegions())
                .add(linkTo(methodOn(getClass()).listDatabaseRegions())
                        .withSelfRel());
    }

//    @PostMapping(value = "/sync")
//    @TransactionBoundary
//    public ResponseEntity<MessageModel> syncRegions() {
//        metadataRepository.syncRegions();
//        return ResponseEntity.ok(new MessageModel("")
//                .add(linkTo(methodOn(getClass()).syncRegions()).withSelfRel())
//        );
//    }

    //    @PostMapping(value = "/regions")
//    @TransactionBoundary
//    public ResponseEntity<EntityModel<Region>> addRegion(@Valid @RequestBody Region form) {
//        Region region = metadataRepository.addRegion(form.getName(), form.getCityGroups());
//        return ResponseEntity.status(HttpStatus.CREATED).body(regionResourceAssembler.toModel(region));
//    }

//    @DeleteMapping(value = "/regions/{region}")
//    @TransactionBoundary(readOnly = true)
//    public ResponseEntity<String> deleteRegion(
//            @PathVariable("region") String region) {
//        metadataRepository.deleteRegion(region);
//        return ResponseEntity.ok("Deleted " + region);
//    }

}
