package io.roach.bank.web.push;

import io.roach.bank.api.AccountSummary;
import io.roach.bank.api.ReportUpdate;
import io.roach.bank.api.TransactionSummary;
import io.roach.bank.ApplicationModel;
import io.roach.bank.repository.ReportingRepository;
import io.roach.bank.util.ConcurrencyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.cockroachdb.annotations.Retryable;
import org.springframework.data.cockroachdb.annotations.TimeTravel;
import org.springframework.data.cockroachdb.annotations.TimeTravelMode;
import org.springframework.data.cockroachdb.annotations.TransactionBoundary;
import org.springframework.data.cockroachdb.annotations.TransactionPriority;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ReportPublisher {
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    private ApplicationModel applicationModel;

    @Autowired
    @Lazy
    private ReportPublisher selfProxy;

    @Autowired
    private ReportingRepository reportingRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Async
    public void publishSummaryAsync(Set<String> cities) {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "TX active");

        if (lock.tryLock()) {
            try {
                List<Callable<Boolean>> tasks = Collections.synchronizedList(new ArrayList<>());

                cities.forEach(city -> {
                    tasks.add(() -> {
                        selfProxy.computeSummaryAndPush(city);
                        return true;
                    });
                });

                // Retrieve accounts per region concurrently with a collective timeout
                int completions = ConcurrencyUtils.runConcurrentlyAndWait(tasks,
                        applicationModel.getReportQueryTimeout(), TimeUnit.SECONDS);

                ReportUpdate reportUpdate = new ReportUpdate();
                reportUpdate.setLastUpdatedAt(LocalDateTime.now());
                reportUpdate.setNumCities(completions);
                reportUpdate.setMessage("Last updated at " + LocalDateTime.now()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + " - " + completions + " cities");

                simpMessagingTemplate.convertAndSend(TopicNames.TOPIC_REPORT_UPDATE, reportUpdate);
            } finally {
                // Send empty message to mark completion
                simpMessagingTemplate.convertAndSend(TopicNames.TOPIC_TRANSACTION_SUMMARY, "");

                lock.unlock();
            }
        }
    }

    @TransactionBoundary(readOnly = true,
            timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ),
            priority = TransactionPriority.LOW)
    @Retryable
    public void computeSummaryAndPush(String city) {
        AccountSummary accountSummary = reportingRepository.accountSummary(city);
        simpMessagingTemplate.convertAndSend(TopicNames.TOPIC_ACCOUNT_SUMMARY, accountSummary);

        TransactionSummary transactionSummary = reportingRepository.transactionSummary(city);
        simpMessagingTemplate.convertAndSend(TopicNames.TOPIC_TRANSACTION_SUMMARY, transactionSummary);
    }
}
