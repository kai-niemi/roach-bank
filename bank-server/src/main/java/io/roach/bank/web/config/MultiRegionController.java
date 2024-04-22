package io.roach.bank.web.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.MessageModel;
import io.roach.bank.api.Region;
import io.roach.bank.api.SurvivalGoal;
import io.roach.bank.repository.MultiRegionRepository;
import io.roach.bank.repository.RegionRepository;

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
                .setSurvivalGaol(null))
                .withRel("survival-goal")
                .withTitle("Configure survival goal"));

        index.add(linkTo(methodOn(getClass())
                .enableMultiRegion())
                .withRel("enable-multiregion")
                .withTitle("Enable multi-region table localities"));
        index.add(linkTo(methodOn(getClass())
                .disableMultiRegion())
                .withRel("disable-multiregion")
                .withTitle("Disable multi-region table localities"));

        return ResponseEntity.ok().body(index);
    }

    @PutMapping(value = "/regions")
    public ResponseEntity<MessageModel> addDatabaseRegions() {
        List<Region> regions = metadataRepository.listRegions(List.of());

        multiRegionRepository.addDatabaseRegions(regions);

        MessageModel model = new MessageModel();
        model.setMessage("Added regions: " +
                StringUtils.collectionToCommaDelimitedString(regions.stream()
                        .map(Region::getName).collect(Collectors.toList())));

        return ResponseEntity.ok(model);
    }

    @DeleteMapping(value = "/regions")
    public ResponseEntity<MessageModel> dropDatabaseRegions() {
        List<Region> regions = metadataRepository.listRegions(List.of());

        multiRegionRepository.dropDatabaseRegions(regions);

        MessageModel model = new MessageModel();
        model.setMessage("Dropped regions: " +
                StringUtils.collectionToCommaDelimitedString(regions.stream()
                        .map(Region::getName).collect(Collectors.toList())));

        return ResponseEntity.ok(model);
    }

    @PutMapping(value = "/primary/{region}")
    public ResponseEntity<MessageModel> setPrimaryRegion(@PathVariable("region") String region) {
        Region r = metadataRepository.getRegionByName(region);
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
        Region r = metadataRepository.getRegionByName(region);
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

    @PostMapping(value = "/enable")
    public ResponseEntity<MessageModel> enableMultiRegion() {
        multiRegionRepository.addDatabaseRegions(metadataRepository.listRegions(List.of()));

        multiRegionRepository.setGlobalTable("region");
        multiRegionRepository.setGlobalTable("region_mapping");
        multiRegionRepository.setRegionalByRowTable("account");
        multiRegionRepository.setRegionalByRowTable("transaction");
        multiRegionRepository.setRegionalByRowTable("transaction_item");
        multiRegionRepository.setSurvivalGoal(SurvivalGoal.REGION);

        logger.info("Multi-region localities added");

        MessageModel model = new MessageModel();
        model.setMessage("Configured table localities for multi-region (global and RBR)");

        return ResponseEntity.ok(model);
    }

    @PostMapping(value = "/disable")
    public ResponseEntity<MessageModel> disableMultiRegion() {
        multiRegionRepository.setRegionalByTable("region");
        multiRegionRepository.setRegionalByTable("region_mapping");
        multiRegionRepository.setRegionalByTable("account");
        multiRegionRepository.setRegionalByTable("transaction");
        multiRegionRepository.setRegionalByTable("transaction_item");
        multiRegionRepository.setSurvivalGoal(SurvivalGoal.ZONE);

        multiRegionRepository.dropDatabaseRegions(metadataRepository.listRegions(List.of()));

        logger.info("Multi-region localities removed");

        MessageModel model = new MessageModel();
        model.setMessage("Removed table localities for multi-region (global and RBR)");

        return ResponseEntity.ok(model);
    }

    @PutMapping(value = "/survival/{goal}")
    public ResponseEntity<MessageModel> setSurvivalGaol(@PathVariable("goal") SurvivalGoal goal) {
        multiRegionRepository.setSurvivalGoal(goal);

        MessageModel model = new MessageModel();
        model.setMessage("Set survival goal: " + goal);

        return ResponseEntity.ok(model);
    }

}
