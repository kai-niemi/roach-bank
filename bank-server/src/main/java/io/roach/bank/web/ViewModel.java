package io.roach.bank.web;

import io.roach.bank.domain.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ViewModel {
    private String title;

    private String gatewayRegion;

    private String viewRegion;

    private boolean viewingGatewayRegion;

    private String randomFact;

    private Map<String, List<Region>> regionGroups = new TreeMap<>();

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

    public void addRegion(Region region) {
        regionGroups.computeIfAbsent(region.getProvider(), regions -> new ArrayList<>()).add(region);
    }
}
