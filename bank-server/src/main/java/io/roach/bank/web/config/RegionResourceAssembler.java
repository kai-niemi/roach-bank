package io.roach.bank.web.config;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.Region;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegionResourceAssembler implements SimpleRepresentationModelAssembler<Region> {
    @Override
    public void addLinks(EntityModel<Region> resource) {
        Region region = resource.getContent();
        resource.add(linkTo(methodOn(RegionController.class)
                .getRegion(region.getName()))
                .withSelfRel()
        );
        resource.add(linkTo(methodOn(RegionController.class)
                .listAllCities(region.getName()))
                .withRel(LinkRelations.CITY_LIST_REL));
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Region>> resources) {
    }
}


