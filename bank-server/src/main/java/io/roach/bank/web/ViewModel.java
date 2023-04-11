package io.roach.bank.web;

import io.roach.bank.api.Region;

import java.util.ArrayList;
import java.util.List;

public class ViewModel {
    private int limit;

    private String gatewayRegion;

    private String viewRegion;

    private boolean viewingGatewayRegion;

    private String randomFact;

    private List<Region> regions = new ArrayList<>();

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
