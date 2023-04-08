package io.roach.bank.web;

import io.roach.bank.api.CityGroup;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CityGroupAssembler implements SimpleRepresentationModelAssembler<CityGroup> {
    @Override
    public void addLinks(EntityModel<CityGroup> resource) {
        CityGroup cityGroup = resource.getContent();

        resource.add(linkTo(methodOn(MetadataController.class)
                .getCityGroup(cityGroup.getName())).withSelfRel()
                .andAffordance(afford(methodOn(MetadataController.class)
                        .updateCityGroup(null)))
        );
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<CityGroup>> resources) {
        resources.add(linkTo(methodOn(MetadataController.class)
                .listCityGroups())
                .withSelfRel());
    }
}
