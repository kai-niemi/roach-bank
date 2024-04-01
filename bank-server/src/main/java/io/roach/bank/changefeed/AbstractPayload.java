package io.roach.bank.changefeed;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = AccountPayload.class)
public abstract class AbstractPayload {
    private List<String> key = new ArrayList<>();

    private String topic;

    private String updated;

    public List<String> getKey() {
        return key;
    }

    public String getTopic() {
        return topic;
    }

    public String getUpdated() {
        return updated;
    }
}
