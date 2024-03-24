package io.roach.bank.web.config;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.Region;
import io.roach.bank.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/config/region")
public class RegionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RegionRepository metadataRepository;

    @Autowired
    private RegionResourceAssembler regionResourceAssembler;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Region Configurations");

        index.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel()
                .withTitle("Region metadata"));
        index.add(linkTo(methodOn(getClass())
                .listAllRegions())
                .withRel(LinkRelations.REGION_LIST_REL)
                .withTitle("List all regions"));
        index.add(linkTo(methodOn(getClass())
                .listAllCities(null))
                .withRel(LinkRelations.CITY_LIST_REL)
                .withTitle("List all cities"));
        index.add(linkTo(methodOn(getClass())
                .getRegion(null))
                .withRel(LinkRelations.REGION_REL));
        index.add(linkTo(methodOn(RegionController.class)
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Local gateway region"));

        return ResponseEntity.ok().body(index);
    }

    @GetMapping(value = "/all")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<Region>> listAllRegions() {
        return regionResourceAssembler.toCollectionModel(metadataRepository.listRegions(List.of()));
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public Set<String> listAllCities(@RequestParam(value = "region", required = false, defaultValue = "all") String region) {
        List<String> regions = "gateway".equals(region)
                ? List.of(metadataRepository.getGatewayRegion())
                : "all".equals(region)
                ? List.of() : List.of(region);
        List<Region> regionList = metadataRepository.listRegions(regions);
        return metadataRepository.listCities(regionList);
    }

    @GetMapping(value = "/{region}")
    @TransactionBoundary(readOnly = true)
    public EntityModel<Region> getRegion(@PathVariable("region") String region) {
        return regionResourceAssembler.toModel(metadataRepository.getRegionByName(region));
    }

    @GetMapping(value = "/gateway")
    @TransactionBoundary(readOnly = true)
    public Map<String, String> gatewayRegion() {
        return Collections.singletonMap("region", metadataRepository.getGatewayRegion());
    }
}
