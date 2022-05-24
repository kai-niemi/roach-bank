package io.roach.bank.api;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionPoolConfig extends RepresentationModel<ConnectionPoolConfig> {
    public long connectionTimeout;

    public String poolName;

    public long idleTimeout;

    public long leakDetectionThreshold;

    public int maximumPoolSize;

    public long maxLifetime;

    public int minimumIdle;

    public long validationTimeout;

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getPoolName() {
        return poolName;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public long getValidationTimeout() {
        return validationTimeout;
    }
}
