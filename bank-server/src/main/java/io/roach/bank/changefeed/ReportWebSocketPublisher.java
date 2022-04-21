package io.roach.bank.changefeed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.config.CacheConfig;
import io.roach.bank.repository.MetadataRepository;
import io.roach.bank.repository.ReportingRepository;
import io.roach.bank.util.TimeBoundExecution;

@Service
public class ReportWebSocketPublisher {
    public static final String TOPIC_ACCOUNT_SUMMARY = "/topic/account-summary";

    public static final String TOPIC_TRANSACTION_SUMMARY = "/topic/transaction-summary";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReentrantLock lock = new ReentrantLock();

    @Value("${roachbank.reportQueryTimeout}")
    private int queryTimeout = 120;

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

    @Scheduled(fixedRateString = "${roachbank.reportPushInterval}", initialDelayString = "${roachbank.reportPushInterval}")
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

                metadataRepository.getCurrencies().forEach((currency) -> {
                    tasks.add(() -> {
                        logger.trace("Processing {}", currency);
                        selfProxy.computeSummaryAndPush(currency);
                        return null;
                    });
                });

                TimeBoundExecution.runConcurrently(tasks, queryTimeout, TimeUnit.MILLISECONDS);
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
            timeTravel = @TimeTravel(mode = TimeTravelMode.SNAPSHOT_READ, interval = "-10s"),
            vectorize = TransactionBoundary.Vectorize.off,
            priority = TransactionBoundary.Priority.low)
    public void computeSummaryAndPush(Currency currency) {
        AccountSummary accountSummary = reportingRepository.accountSummary(currency);
        simpMessagingTemplate.convertAndSend(TOPIC_ACCOUNT_SUMMARY, accountSummary);

        TransactionSummary transactionSummary = reportingRepository.transactionSummary(currency);
        simpMessagingTemplate.convertAndSend(TOPIC_TRANSACTION_SUMMARY, transactionSummary);
    }
}
