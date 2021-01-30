package io.roach.bank.web.api;

import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.BankLinkRelations;
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

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass())
                .regionGroups())
                .withRel(BankLinkRelations.REGION_GROUPS_REL)
                .withTitle("Region group aliases"));

        index.add(linkTo(methodOn(getClass())
                .regionCurrency(null))
                .withRel(BankLinkRelations.REGION_CURRENCIES_REL)
                .withTitle("Region currencies"));

        index.add(linkTo(methodOn(getClass())
                .allCurrencies())
                .withRel(BankLinkRelations.CURRENCIES_REL)
                .withTitle("All currencies"));

        index.add(linkTo(methodOn(getClass())
                .allRegions())
                .withRel(BankLinkRelations.REGIONS_REL)
                .withTitle("All regions"));

        index.add(linkTo(methodOn(getClass())
                .localRegions())
                .withRel(BankLinkRelations.LOCAL_REGIONS_REL)
                .withTitle("Local regions"));

        return index;
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

    @GetMapping(value = "/local-regions")
    @TransactionBoundary(readOnly = true)
    public List<String> localRegions() {
        return metadataRepository.getLocalRegions();
    }
}
