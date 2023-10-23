package io.roach.bank.web;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.roach.bank.api.CityGroup;
import io.roach.bank.api.LinkRelations;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CityGroupAssembler implements SimpleRepresentationModelAssembler<CityGroup> {
    @Override
    public void addLinks(EntityModel<CityGroup> resource) {
        CityGroup cityGroup = resource.getContent();

        resource.add(linkTo(methodOn(CityGroupController.class)
                .getCityGroup(cityGroup.getName())).withSelfRel()
                .andAffordance(afford(methodOn(CityGroupController.class)
                        .updateCityGroup(cityGroup.getName(), null )))
        );
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<CityGroup>> resources) {
        resources.add(linkTo(methodOn(CityGroupController.class)
                .index())
                .withSelfRel());
        resources.add(linkTo(methodOn(CityGroupController.class)
                .listCities(null))
                .withRel(LinkRelations.CITY_LIST_REL)
                .withTitle("List cities grouped by region(s)"));
    }
}
