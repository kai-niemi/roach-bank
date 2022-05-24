package io.roach.bank.web.support;

import com.zaxxer.hikari.HikariPoolMXBean;

import io.roach.bank.api.ConnectionPoolSize;

public abstract class ConnectionPoolSizeFactory {
    private ConnectionPoolSizeFactory() {
    }

    public static ConnectionPoolSize from(HikariPoolMXBean bean) {
        ConnectionPoolSize instance = new ConnectionPoolSize();
        instance.activeConnections = bean.getActiveConnections();
        instance.idleConnections = bean.getIdleConnections();
        instance.threadsAwaitingConnection = bean.getThreadsAwaitingConnection();
        instance.totalConnections = bean.getTotalConnections();
        return instance;
    }
}
