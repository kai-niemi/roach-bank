package io.roach.bank.web.report;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.Region;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.changefeed.ReportPublisher;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.repository.RegionRepository;
import io.roach.bank.repository.ReportingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TimeTravelMode;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.annotations.TransactionPriority;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/report")
public class ReportController {
    @Autowired
    private ReportingRepository reportingRepository;

    @Autowired
    private RegionRepository metadataRepository;

    @Autowired
    private ReportPublisher reportPublisher;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping
    public MessageModel index() {
        MessageModel index = new MessageModel();

        index.add(linkTo(methodOn(getClass())
                .getAccountSummary(null))
                .withRel(LinkRelations.ACCOUNT_SUMMARY_REL)
                .withTitle("Account business report"));

        index.add(linkTo(methodOn(getClass())
                .getTransactionSummary(null))
                .withRel(LinkRelations.TRANSACTION_SUMMARY_REL)
                .withTitle("Transaction business report"));

        return index;
    }

    @GetMapping(value = "/account-summary")
    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.HISTORICAL_READ, interval = "-10s"),
            priority = TransactionPriority.LOW)
    @Retryable
    public Collection<AccountSummary> getAccountSummary(
            @RequestParam(value = "region", defaultValue = "gateway") String region
    ) {
        List<String> regions = "gateway".equals(region)
                ? List.of(metadataRepository.getGatewayRegion())
                : "all".equals(region)
                ? List.of() : List.of(region);

        List<Region> regionList = metadataRepository.listRegions(regions);
        Collection<String> cities = metadataRepository.listCities(regionList);

        Collection<AccountSummary> result = new LinkedList<>();
        cities.forEach((city) -> result.add(reportingRepository.accountSummary(city)));

        return result;
    }

    @GetMapping(value = "/transaction-summary")
    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ),
            priority = TransactionPriority.LOW)
    @Retryable
    public Collection<TransactionSummary> getTransactionSummary(
            @RequestParam(value = "region", defaultValue = "gateway") String region
    ) {
        List<String> regions = "gateway".equals(region)
                ? List.of(metadataRepository.getGatewayRegion())
                : "all".equals(region)
                ? List.of() : List.of(region);

        Collection<String> cities = metadataRepository.listCities(metadataRepository.listRegions(regions));

        Collection<TransactionSummary> result = new LinkedList<>();
        cities.forEach((city) -> result.add(reportingRepository.transactionSummary(city)));

        return result;
    }

    @GetMapping("/refresh")
    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ),
            priority = TransactionPriority.LOW)
    public ResponseEntity<String> refreshReport(
            @RequestParam(value = "region", defaultValue = "gateway") String region) {
        List<String> regions = "gateway".equals(region)
                ? List.of(metadataRepository.getGatewayRegion())
                : "all".equals(region)
                ? List.of() : List.of(region);
        List<Region> regionList = metadataRepository.listRegions(regions);

        // Evict caches since it's a user-initated request
        cacheManager.getCache(CacheConfig.CACHE_ACCOUNT_REPORT_SUMMARY).clear();
        cacheManager.getCache(CacheConfig.CACHE_TRANSACTION_REPORT_SUMMARY).clear();

        reportPublisher.publishSummaryAsync(metadataRepository.listCities(regionList));

        return ResponseEntity.ok().build();
    }
}
