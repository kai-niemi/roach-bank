package io.roach.bank.api;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class RegionConfig {
    private String region;

    private Currency currency;

    private List<String> groupNames = new ArrayList<>();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }
}
