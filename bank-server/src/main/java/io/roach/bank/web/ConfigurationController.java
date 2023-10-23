package io.roach.bank.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/config")
public class ConfigurationController {
    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Configuration resources");

        index.add(linkTo(methodOn(RegionController.class)
                .index())
                .withRel(LinkRelations.CONFIG_REGION_REL)
                .withTitle("Region configuration")
        );
        index.add(linkTo(methodOn(MultiRegionController.class)
                .index())
                .withRel(LinkRelations.CONFIG_MULTI_REGION_REL)
                .withTitle("Multi-region configuration")
        );
        index.add(linkTo(methodOn(CityGroupController.class)
                .index())
                .withRel(LinkRelations.CONFIG_CITY_GROUP_REL)
                .withTitle("City group configuration")
        );

        return ResponseEntity.ok(index);
    }
}
