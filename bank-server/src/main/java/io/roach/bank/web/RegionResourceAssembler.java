package io.roach.bank.web;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.Region;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class RegionResourceAssembler implements SimpleRepresentationModelAssembler<Region> {
    @Override
    public void addLinks(EntityModel<Region> resource) {
        Region region = resource.getContent();

        resource.add(linkTo(methodOn(RegionController.class)
                .getRegion(region.getName())).withSelfRel()
                .andAffordance(afford(methodOn(RegionController.class)
                        .updateRegion(null)))
                .andAffordance(afford(methodOn(RegionController.class)
                        .deleteRegion(null)))
        );

        region.getCityGroups().forEach(cg -> {
            resource.add(linkTo(methodOn(CityGroupController.class)
                    .getCityGroup(cg))
                    .withRel(LinkRelations.CONFIG_CITY_GROUP_REL)
            );
        });
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Region>> resources) {
        resources.add(linkTo(methodOn(RegionController.class)
                .index())
                .withSelfRel());
        resources.add(linkTo(methodOn(RegionController.class)
                .gatewayRegion())
                .withRel(LinkRelations.GATEWAY_REGION_REL)
                .withTitle("Local gateway region"));
    }
}


