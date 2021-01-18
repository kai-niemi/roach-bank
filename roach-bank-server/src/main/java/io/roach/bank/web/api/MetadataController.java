package io.roach.bank.web.api;

import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.annotation.TransactionNotSupported;
import io.roach.bank.api.BankLinkRelations;
import io.roach.bank.api.RegionConfig;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.web.support.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/metadata")
public class MetadataController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MetadataRepository metadataRepository;

    @Value("${roachbank.region}")
    private String region;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass())
                .regionGroups())
                .withRel(BankLinkRelations.REGION_GROUPS_REL)
                .withTitle("Region group aliases"));

        index.add(Link.of(UriTemplate.of(linkTo(MetadataController.class)
                        .toUriComponentsBuilder().path(
                        "/region-currency/{?regions}")  // RFC-6570 template
                        .build().toUriString()),
                BankLinkRelations.REGION_CURRENCY_REL
        ).withTitle("Region currency"));

        index.add(linkTo(methodOn(getClass())
                .allCurrencies())
                .withRel(BankLinkRelations.CURRENCIES_REL)
                .withTitle("All currencies"));

        index.add(linkTo(methodOn(getClass())
                .allRegions())
                .withRel(BankLinkRelations.REGIONS_REL)
                .withTitle("All regions"));

        index.add(linkTo(methodOn(getClass())
                .getRegionConfigs())
                .withRel(BankLinkRelations.REGION_CONFIG_REL)
                .withTitle("All region mappings"));

        index.add(linkTo(methodOn(getClass())
                .regionName())
                .withRel(BankLinkRelations.REGION_NAME_REL)
                .withTitle("Local app region name"));

        return index;
    }

    @GetMapping(value = "/region")
    public ResponseEntity<String> regionName() {
        return ResponseEntity.ok(region);
    }

    @GetMapping(value = "/region-groups")
    @TransactionBoundary(readOnly = true)
    public Map<String, List<String>> regionGroups() {
        return metadataRepository.getGroupRegions();
    }

    @GetMapping(value = "/region-currency")
    @TransactionBoundary(readOnly = true)
    public Map<String, Currency> regionCurrency(
            @RequestParam(name = "regions", required = false, defaultValue = "") List<String> regions) {
        if (regions.isEmpty()) {
            regions = metadataRepository.getRegions();
        }
        return metadataRepository.resolveRegions(regions);
    }

    @GetMapping(value = "/currencies")
    @TransactionBoundary(readOnly = true)
    public List<Currency> allCurrencies() {
        return metadataRepository.getCurrencies();
    }

    @GetMapping(value = "/regions")
    @TransactionBoundary(readOnly = true)
    public List<String> allRegions() {
        return metadataRepository.getRegions();
    }

    @GetMapping(value = "/region-config")
    @TransactionBoundary(readOnly = true)
    public List<RegionConfig> getRegionConfigs() {
        return metadataRepository.getRegionConfigs();
    }
}
