package io.roach.bank.changefeed.egress;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.aspect.TimeTravelMode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.ReportUpdate;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.repository.ReportingRepository;
import io.roach.bank.util.ConcurrencyUtils;

@Service
public class ReportWebSocketPublisher {
    public static final String TOPIC_ACCOUNT_SUMMARY = "/topic/account-summary";

    public static final String TOPIC_TRANSACTION_SUMMARY = "/topic/transaction-summary";

    public static final String TOPIC_REPORT_UPDATE = "/topic/report-update";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReentrantLock lock = new ReentrantLock();

    @Value("${roachbank.reportQueryTimeoutSeconds}")
    private int reportQueryTimeoutSeconds = 120;

    @Autowired
    @Lazy
    private ReportWebSocketPublisher selfProxy;

    @Autowired
    private ReportingRepository reportingRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRateString = "${roachbank.reportPushInterval}",
            initialDelayString = "${roachbank.reportPushInterval}",
            timeUnit = TimeUnit.SECONDS)
    public void publishReport() {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_ACCOUNT_REPORT_SUMMARY);
        ConcurrentHashMap map = (ConcurrentHashMap) cache.getNativeCache();
        // Only publish if cache is warm and no scan is already in progress
        if (!map.isEmpty() && !lock.isLocked()) {
            publishSummaryAsync();
        }
    }

    @Async
    public void publishSummaryAsync() {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "TX active");

        if (lock.tryLock()) {
            try {
                logger.trace(">> Event publisher lock acquired");

                // Retrieve accounts per region concurrently with a collective timeout
                List<Callable<Void>> tasks = Collections.synchronizedList(new ArrayList<>());

                Set<String> cities = metadataRepository.getRegionCities(Collections.emptyList());

                cities.forEach(city -> {
                    tasks.add(() -> {
                        selfProxy.computeSummaryAndPush(city);
                        return null;
                    });
                });

                ConcurrencyUtils.runConcurrentlyAndWait(tasks, reportQueryTimeoutSeconds, TimeUnit.SECONDS);

                ReportUpdate reportUpdate = new ReportUpdate() ;
                reportUpdate.setLastUpdatedAt(LocalDateTime.now());
                reportUpdate.setNumCities(cities.size());
                reportUpdate.setMessage("Last updated " + LocalDateTime.now()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                simpMessagingTemplate.convertAndSend(TOPIC_REPORT_UPDATE, reportUpdate);
            } finally {
                // Send empty message to mark completion
                simpMessagingTemplate.convertAndSend(TOPIC_TRANSACTION_SUMMARY, "");

                lock.unlock();
                logger.trace("<< Event publisher lock released");
            }
        } else {
            logger.trace("<< Event publisher lock already acquired");
        }
    }

    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.HISTORICAL_READ, interval = "-10s"),
            priority = TransactionBoundary.Priority.low)
    @Retryable
    public void computeSummaryAndPush(String city) {
        AccountSummary accountSummary = reportingRepository.accountSummary(city);
        simpMessagingTemplate.convertAndSend(TOPIC_ACCOUNT_SUMMARY, accountSummary);

        TransactionSummary transactionSummary = reportingRepository.transactionSummary(city);
        simpMessagingTemplate.convertAndSend(TOPIC_TRANSACTION_SUMMARY, transactionSummary);
    }
}
