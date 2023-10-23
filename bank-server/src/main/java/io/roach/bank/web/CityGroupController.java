package io.roach.bank.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.CityGroup;
import io.roach.bank.repository.RegionRepository;

@RestController
@RequestMapping(value = "/api/config/citygroup")
public class CityGroupController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CityGroupAssembler cityGroupAssembler;

    @Autowired
    private RegionRepository metadataRepository;

    @GetMapping
    @TransactionBoundary(readOnly = true)
    public CollectionModel<EntityModel<CityGroup>> index() {
        return cityGroupAssembler.toCollectionModel(metadataRepository.listCityGroups());
    }

    @PutMapping(value = "/{name}")
    @TransactionBoundary
    public EntityModel<CityGroup> updateCityGroup(@PathVariable("name") String name,
                                                  @RequestBody CityGroup cityGroup) {
        CityGroup group = metadataRepository.getCityGroup(name);
        if (group == null) {
            throw new ObjectRetrievalFailureException(CityGroup.class, name);
        }
        group.setCities(cityGroup.getCities());
        return cityGroupAssembler.toModel(metadataRepository.updateCityGroup(group));
    }

    @GetMapping(value = "/{name}")
    @TransactionBoundary(readOnly = true)
    public EntityModel<CityGroup> getCityGroup(@PathVariable("name") String name) {
        return cityGroupAssembler.toModel(metadataRepository.getCityGroup(name));
    }

    @GetMapping(value = "/cities")
    @TransactionBoundary(readOnly = true)
    public CollectionModel<String> listCities(
            @RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        return CollectionModel.of(metadataRepository.listCities(regions));
    }

}
