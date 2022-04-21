package io.roach.bank.changefeed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangeFeedEvent<T extends AbstractPayload> {
    @JsonProperty("payload")
    private List<T> payload;

    private String resolved;

    public List<T> getPayload() {
        return payload;
    }

    public String getResolved() {
        return resolved;
    }
}
