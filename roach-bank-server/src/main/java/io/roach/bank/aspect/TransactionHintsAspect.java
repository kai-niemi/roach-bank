package io.roach.bank.aspect;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.roach.bank.ProfileNames;
import io.roach.bank.annotation.TimeTravel;
import io.roach.bank.annotation.TimeTravelMode;
import io.roach.bank.annotation.TransactionBoundary;
import io.roach.bank.annotation.TransactionHint;
import io.roach.bank.annotation.TransactionHints;

/**
 * This advisor must be after retry and TX advisors in the call chain (in a transactional context)
 */
@Aspect
@Component
@Order(AdvisorOrder.TX_ATTRIBUTES_ADVISOR)
@Profile(ProfileNames.DB_COCKROACH)
public class TransactionHintsAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${info.build.app-label}")
    private String applicationName;

    private boolean enterpriseLicenseFound;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping Transaction Hints");

        String license = jdbcTemplate.queryForObject("SHOW CLUSTER SETTING enterprise.license", String.class);

        if (StringUtils.hasLength(license)) {
            String org = jdbcTemplate.queryForObject("SHOW CLUSTER SETTING cluster.organization", String.class);
            logger.info("Found CockroachDB Enterprise Licence for {}", org);
        }
    }

    @Around(value = "io.roach.bank.aspect.Pointcuts.anyTransactionBoundaryOperation(transactionBoundary)",
            argNames = "pjp,transactionBoundary")
    public Object doInTransaction(ProceedingJoinPoint pjp, TransactionBoundary transactionBoundary)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");

        // Grab from type if needed (for non-annotated methods)
        if (transactionBoundary == null) {
            transactionBoundary = AopSupport.findAnnotation(pjp, TransactionBoundary.class);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Transaction attributes applied for {}: {}",
                    pjp.getSignature().toShortString(),
                    transactionBoundary);
        }

        applyVariables(transactionBoundary);

        return pjp.proceed();
    }

    private void applyVariables(TransactionBoundary transactionBoundary) {
        if ("(default)".equals(transactionBoundary.applicationName())) {
            jdbcTemplate.update("SET application_name=?", applicationName);
        } else if (!"".equals(transactionBoundary.applicationName())) {
            jdbcTemplate.update("SET application_name=?", transactionBoundary.applicationName());
        }

        if (!TransactionBoundary.Priority.normal.equals(transactionBoundary.priority())) {
            jdbcTemplate.execute("SET TRANSACTION PRIORITY " + transactionBoundary.priority().name());
        }

        if (!TransactionBoundary.Vectorize.auto.equals(transactionBoundary.vectorize())) {
            jdbcTemplate.execute("SET vectorize='" + transactionBoundary.vectorize().name() + "'");
        }

        if (transactionBoundary.timeout() > 0) {
            jdbcTemplate.update("SET statement_timeout=?", transactionBoundary.timeout() * 1000);
        }

//        if (transactionBoundary.readOnly()) {
//            jdbcTemplate.execute("SET transaction_read_only=true");
//        }

        TimeTravel timeTravel = transactionBoundary.timeTravel();

        if (timeTravel.mode().equals(TimeTravelMode.FOLLOWER_READ) && enterpriseLicenseFound) {
            jdbcTemplate.execute("SET TRANSACTION AS OF SYSTEM TIME experimental_follower_read_timestamp()");
        } else if (timeTravel.mode().equals(TimeTravelMode.SNAPSHOT_READ)) {
            jdbcTemplate.update("SET TRANSACTION AS OF SYSTEM TIME INTERVAL '"
                    + timeTravel.interval() + "'");
        }
    }

    @Around(value = "io.roach.bank.aspect.Pointcuts.anyTransactionHintedOperation(transactionHints)",
            argNames = "pjp,transactionHints")
    public Object doInTransactionHinted(ProceedingJoinPoint pjp, TransactionHints transactionHints)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active");

        if (logger.isTraceEnabled()) {
            logger.trace("Transaction hints applied for {}: {}",
                    pjp.getSignature().toShortString(),
                    transactionHints);
        }

        for (TransactionHint hint : transactionHints.value()) {
            if (hint.intValue() >= 0) {
                jdbcTemplate.update("SET " + hint.name() + "=" + hint.intValue());
            } else {
                jdbcTemplate.update("SET " + hint.name() + "=?", hint.value());
            }
        }

        return pjp.proceed();
    }
}
