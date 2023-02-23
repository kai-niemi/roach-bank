package io.roach.bank.service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.roach.bank.AdvisorOrder;
import io.roach.bank.ProfileNames;
import io.roach.bank.domain.Transaction;

@Aspect
@Component
@Order(AdvisorOrder.CHANGE_FEED_ADVISOR)
@Profile(ProfileNames.OUTBOX)
public class OutboxAspect {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        logger.info("Bootstrapping outbox aspect (transaction root aggregate)");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterReturning(pointcut = "execution(* io.roach.bank.service.DefaultTransactionService.createTransaction(..))",
            returning = "transaction")
    @Transactional(propagation = Propagation.MANDATORY)
    public void doAfterTransaction(Transaction transaction) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No transaction context - check Spring profile settings");
        }

        try {
            String payload = mapper.writer().writeValueAsString(transaction);

            jdbcTemplate.update("INSERT INTO outbox (aggregate_type,aggregate_id,event_type,payload) VALUES (?,?,?,?)",
                    ps -> {
                        ps.setString(1, "transaction");
                        ps.setString(2, transaction.getId().toString());
                        ps.setString(3, "TransactionCreated");
                        ps.setObject(4, payload);
                    });
        } catch (JsonProcessingException e) {
            throw new InfrastructureException("Error serializing outbox JSON payload", e);
        }
    }
}

