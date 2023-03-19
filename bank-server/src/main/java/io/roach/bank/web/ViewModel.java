package io.roach.bank.web;

import java.util.List;
import java.util.Map;

import io.roach.bank.domain.Region;

public class ViewModel {
    private String title;

    private String gatewayRegion;

    private String viewRegion;

    private boolean viewingGatewayRegion;

    private String randomFact;

    private Map<String, List<Region>> regionGroups;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Map<String, List<Region>> getRegionGroups() {
        return regionGroups;
    }

    public void setRegionGroups(Map<String, List<Region>> regionGroups) {
        this.regionGroups = regionGroups;
    }
}
