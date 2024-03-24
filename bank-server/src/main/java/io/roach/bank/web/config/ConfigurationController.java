package io.roach.bank.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.ProfileNames;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/config")
public class ConfigurationController {
    @Autowired
    private Environment environment;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = new MessageModel();
        index.setMessage("Configuration resources");

        index.add(linkTo(methodOn(RegionController.class)
                .index())
                .withRel(LinkRelations.CONFIG_REGION_REL)
                .withTitle("Region configuration")
        );

        if (!ProfileNames.acceptsPostgresSQL(environment)) {
            index.add(linkTo(methodOn(MultiRegionController.class)
                    .index())
                    .withRel(LinkRelations.CONFIG_MULTI_REGION_REL)
                    .withTitle("Multi-region configuration")
            );
        }

        return ResponseEntity.ok(index);
    }
}
