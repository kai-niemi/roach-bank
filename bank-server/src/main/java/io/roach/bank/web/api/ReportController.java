package io.roach.bank.web.api;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.aspect.TimeTravelMode;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.LinkRelations;
import io.roach.bank.api.MessageModel;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.changefeed.egress.ReportWebSocketPublisher;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.repository.ReportingRepository;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/report")
public class ReportController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ReportingRepository reportingRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private ReportWebSocketPublisher reportPublisher;

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
            priority = TransactionBoundary.Priority.low)
    @Retryable
    public Collection<AccountSummary> getAccountSummary(
            @RequestParam(value = "regions", defaultValue = "", required = false) Set<String> regions
    ) {
        Set<String> cities = metadataRepository.getRegionCities(regions);
        Collection<AccountSummary> result = new LinkedList<>();
        cities.forEach((city) -> {
            result.add(reportingRepository.accountSummary(city));
        });
        return result;
    }

    @GetMapping(value = "/transaction-summary")
    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.HISTORICAL_READ, interval = "-10s"),
            priority = TransactionBoundary.Priority.low)
    @Retryable
    public Collection<TransactionSummary> getTransactionSummary(
            @RequestParam(value = "regions", defaultValue = "", required = false) Set<String> regions
    ) {
        Set<String> cities = metadataRepository.getRegionCities(regions);
        Collection<TransactionSummary> result = new LinkedList<>();
        cities.forEach((city) -> {
            result.add(reportingRepository.transactionSummary(city));
        });
        return result;
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshReport(@RequestParam(value = "region", required = false) String region,
                                                Model model) {
        // Evict caches since its a user-initated request
        cacheManager.getCache(CacheConfig.CACHE_ACCOUNT_REPORT_SUMMARY).clear();
        cacheManager.getCache(CacheConfig.CACHE_TRANSACTION_REPORT_SUMMARY).clear();

        reportPublisher.publishSummaryAsync("null".equals(region) ? null : region);

        return ResponseEntity.ok().build();
    }
}
