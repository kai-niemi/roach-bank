package io.roach.bank.api;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionPoolSize extends RepresentationModel<ConnectionPoolSize> {
    public int activeConnections;

    public int idleConnections;

    public int threadsAwaitingConnection;

    public int totalConnections;

    public int getActiveConnections() {
        return activeConnections;
    }

    public int getIdleConnections() {
        return idleConnections;
    }

    public int getThreadsAwaitingConnection() {
        return threadsAwaitingConnection;
    }

    public int getTotalConnections() {
        return totalConnections;
    }
}
