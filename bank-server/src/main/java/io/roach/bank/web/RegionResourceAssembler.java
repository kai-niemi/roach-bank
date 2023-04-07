package io.roach.bank.web;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.domain.Region;
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
        resource.add(linkTo(methodOn(MetadataController.class)
                .getRegion(region.getProvider(), region.getName())).withSelfRel()
                .andAffordance(afford(methodOn(MetadataController.class)
                        .deleteRegion(region.getProvider(), region.getName())))
        );
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Region>> resources) {
        resources.add(linkTo(methodOn(MetadataController.class)
                .listRegions())
                .withRel(LinkRelations.REGION_LIST_REL));
    }

}


