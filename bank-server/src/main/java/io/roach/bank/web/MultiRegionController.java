package io.roach.bank.web;

import io.roach.bank.api.MessageModel;
import io.roach.bank.api.Region;
import io.roach.bank.domain.SurvivalGoal;
import io.roach.bank.repository.MultiRegionRepository;
import io.roach.bank.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/config/multiregion")
public class MultiRegionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RegionRepository metadataRepository;

    @Autowired
    private MultiRegionRepository multiRegionRepository;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Multi-Region Configurations");

        index.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel()
                .withTitle("Configure multi-region"));

        index.add(linkTo(methodOn(getClass())
                .addDatabaseRegions())
                .withRel("add-regions")
                .withTitle("Add database regions"));

        index.add(linkTo(methodOn(getClass())
                .dropDatabaseRegions())
                .withRel("drop-regions")
                .withTitle("Drop database regions"));

        index.add(linkTo(methodOn(getClass())
                .setPrimaryRegion(null))
                .withRel("primary-region")
                .withTitle("Set primary database regions"));

        index.add(linkTo(methodOn(getClass())
                .setSecondaryRegion(null))
                .withRel("secondary-region")
                .withTitle("Set secondary database regions"));

        index.add(linkTo(methodOn(getClass())
                .dropSecondaryRegion())
                .withRel("secondary-region")
                .withTitle("Drop secondary database regions"));

        index.add(linkTo(methodOn(getClass())
                .configureMultiRegions())
                .withRel("configure-multiregion")
                .withTitle("Configure table localities"));


        return ResponseEntity.ok().body(index);
    }

    @PutMapping(value = "/regions")
    public ResponseEntity<MessageModel> addDatabaseRegions() {
        List<Region> regions = metadataRepository.listRegions();

        multiRegionRepository.addDatabaseRegions(regions);

        MessageModel model = new MessageModel();
        model.setMessage("Added regions: " +
                StringUtils.collectionToCommaDelimitedString(regions.stream()
                        .map(Region::getName).collect(Collectors.toList())));

        return ResponseEntity.ok(model);
    }

    @DeleteMapping(value = "/regions")
    public ResponseEntity<MessageModel> dropDatabaseRegions() {
        List<Region> regions = metadataRepository.listRegions();

        multiRegionRepository.dropDatabaseRegions(regions);

        MessageModel model = new MessageModel();
        model.setMessage("Dropped regions: " +
                StringUtils.collectionToCommaDelimitedString(regions.stream()
                        .map(Region::getName).collect(Collectors.toList())));

        return ResponseEntity.ok(model);
    }

    @PutMapping(value = "/primary/{region}")
    public ResponseEntity<MessageModel> setPrimaryRegion(@PathVariable("region") String region) {
        Region r = metadataRepository.getOrCreateRegionByName(region);
        if (r == null) {
            throw new ObjectRetrievalFailureException(Region.class, region);
        }

        multiRegionRepository.setPrimaryRegion(r);

        MessageModel model = new MessageModel();
        model.setMessage("Set primary region: " + r.getName());

        return ResponseEntity.ok(model);
    }

    @PutMapping(value = "/secondary/{region}")
    public ResponseEntity<MessageModel> setSecondaryRegion(@PathVariable("region") String region) {
        Region r = metadataRepository.getOrCreateRegionByName(region);
        if (r == null) {
            throw new ObjectRetrievalFailureException(Region.class, region);
        }

        multiRegionRepository.setSecondaryRegion(r);

        MessageModel model = new MessageModel();
        model.setMessage("Set secondary region: " + r.getName());

        return ResponseEntity.ok(model);
    }

    @DeleteMapping(value = "/secondary")
    public ResponseEntity<MessageModel> dropSecondaryRegion() {
        multiRegionRepository.dropSecondaryRegion();

        MessageModel model = new MessageModel();
        model.setMessage("Drop secondary region");

        return ResponseEntity.ok(model);
    }

    @PostMapping(value = "/configure")
    public ResponseEntity<MessageModel> configureMultiRegions() {
        List<Region> regions = metadataRepository.listRegions();

        if (regions.size() < 3) {
            logger.warn("Expected at least 3 regions - found {}", regions.size());
        }

        multiRegionRepository.addDatabaseRegions(regions);
        multiRegionRepository.setSurvivalGoal(SurvivalGoal.ZONE);
        multiRegionRepository.addRegionalByRowTable("account");
        multiRegionRepository.addRegionalByRowTable("transaction");
        multiRegionRepository.addRegionalByRowTable("transaction_item");
        multiRegionRepository.addGloalTable("region");
        multiRegionRepository.addGloalTable("city_group");

        MessageModel model = new MessageModel();
        model.setMessage("Configured table localities for multi-region (global and RBR)");

        return ResponseEntity.ok(model);
    }
}
