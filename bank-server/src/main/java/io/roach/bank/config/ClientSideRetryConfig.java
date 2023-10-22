package io.roach.bank.config;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.cockroachdb.aspect.TransactionRetryAspect;
import org.springframework.util.Assert;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.roach.bank.ProfileNames;
import jakarta.annotation.PostConstruct;

/**
 * Transaction management with retries and exponential backoff handled at JDBC Driver level.
 */
@Configuration
@Profile({ProfileNames.RETRY_CLIENT})
public class ClientSideRetryConfig {
    @Autowired
    private Environment environment;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter retryEvents;

    private Counter retryCalls;

    private Timer retryTime;

    @PostConstruct
    public void checkProfiles() {
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_NONE)),
                "Cant have both RETRY_CLIENT and RETRY_NONE");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_SAVEPOINT)),
                "Cant have both RETRY_CLIENT and RETRY_SAVEPOINT");
        Assert.isTrue(!environment.acceptsProfiles(Profiles.of(ProfileNames.RETRY_DRIVER)),
                "Cant have both RETRY_CLIENT and RETRY_DRIVER");

        this.retryEvents = meterRegistry.counter("bank.retries.event");

        this.retryCalls = meterRegistry.counter("bank.retries.call");

        this.retryTime = Timer.builder("bank.retries.timeInRetryLoop")
                .register(meterRegistry);
    }

    @Bean
    public TransactionRetryAspect transactionRetryAspect() {
        TransactionRetryAspect retryAspect = new TransactionRetryAspect() {
            @Override
            protected boolean isRetryable(SQLException sqlException) {
                return PSQLState.SERIALIZATION_FAILURE.getState().equals(sqlException.getSQLState())
                        || PSQLState.DEADLOCK_DETECTED.getState().equals(sqlException.getSQLState());
            }
        };
        retryAspect.setRetryEventConsumer(retryEvent -> {
            this.retryEvents.increment(1);
            this.retryCalls.increment(retryEvent.getNumCalls());
            this.retryTime.record(retryEvent.getElapsedTime().toMillis(), TimeUnit.MILLISECONDS);
        });
        return retryAspect;
    }
}
