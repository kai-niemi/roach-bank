package io.roach.bank.web.support;

import com.zaxxer.hikari.HikariConfigMXBean;

import io.roach.bank.api.ConnectionPoolConfig;

public abstract class ConnectionPoolConfigFactory {
    private ConnectionPoolConfigFactory() {
    }

    public static ConnectionPoolConfig from(HikariConfigMXBean bean) {
        ConnectionPoolConfig instance = new ConnectionPoolConfig();
        instance.connectionTimeout = bean.getConnectionTimeout();
        instance.poolName = bean.getPoolName();
        instance.idleTimeout = bean.getIdleTimeout();
        instance.leakDetectionThreshold = bean.getLeakDetectionThreshold();
        instance.maximumPoolSize = bean.getMaximumPoolSize();
        instance.maxLifetime = bean.getMaxLifetime();
        instance.minimumIdle = bean.getMinimumIdle();
        instance.validationTimeout = bean.getValidationTimeout();
        return instance;
    }
}
