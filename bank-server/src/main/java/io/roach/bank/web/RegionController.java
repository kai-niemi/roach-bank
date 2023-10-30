package io.roach.bank.web;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.Region;
import io.roach.bank.repository.RegionRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/config/region")
public class RegionController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RegionRepository metadataRepository;

    @Autowired
    private RegionResourceAssembler regionResourceAssembler;

    @GetMapping
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<Region>> index() {
        return regionResourceAssembler.toCollectionModel(metadataRepository.listRegions());
    }

    @GetMapping(value = "/{region}")
    @TransactionBoundary(readOnly = true)
    public EntityModel<Region> getRegion(@PathVariable("region") String region) {
        return regionResourceAssembler.toModel(metadataRepository.getRegionByName(region));
    }

    @PutMapping
    @TransactionBoundary
    public EntityModel<Region> updateRegion(@RequestBody Region form) {
        return regionResourceAssembler.toModel(
                metadataRepository.updateRegion(form));
    }

    @DeleteMapping
    @TransactionBoundary
    public ResponseEntity<?> deleteRegion(@RequestBody Region form) {
        metadataRepository.deleteRegion(form.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @TransactionBoundary
    public ResponseEntity<EntityModel<Region>> addRegion(@Valid @RequestBody Region form) {
        Region region = metadataRepository.createRegion(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(regionResourceAssembler.toModel(region));
    }

    @GetMapping(value = "/gateway")
    @TransactionBoundary(readOnly = true)
    public Map<String, String> gatewayRegion() {
        return Collections.singletonMap("region", metadataRepository.getGatewayRegion());
    }
}
