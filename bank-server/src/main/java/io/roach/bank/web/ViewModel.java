package io.roach.bank.web;

import java.util.ArrayList;
import java.util.List;

import io.roach.bank.api.Region;

public class ViewModel {
    private int limit;

    private String gatewayRegion;

    private String primaryRegion;

    private String secondaryRegion;

    private String viewRegion;

    private boolean viewingGatewayRegion;

    private String randomFact;

    private final List<Region> regions = new ArrayList<>();

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getGatewayRegion() {
        return gatewayRegion;
    }

    public void setGatewayRegion(String gatewayRegion) {
        this.gatewayRegion = gatewayRegion;
    }

    public String getPrimaryRegion() {
        return primaryRegion;
    }

    public void setPrimaryRegion(String primaryRegion) {
        this.primaryRegion = primaryRegion;
    }

    public String getSecondaryRegion() {
        return secondaryRegion;
    }

    public void setSecondaryRegion(String secondaryRegion) {
        this.secondaryRegion = secondaryRegion;
    }

    public String getViewRegion() {
        return viewRegion;
    }

    public void setViewRegion(String viewRegion) {
        this.viewRegion = viewRegion;
    }

    public boolean isViewingGatewayRegion() {
        return viewingGatewayRegion;
    }

    public void setViewingGatewayRegion(boolean viewingGatewayRegion) {
        this.viewingGatewayRegion = viewingGatewayRegion;
    }

    public String getRandomFact() {
        return randomFact;
    }

    public void setRandomFact(String randomFact) {
        this.randomFact = randomFact;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void addRegion(Region region) {
        regions.add(region);
    }
}
