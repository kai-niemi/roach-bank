package io.roach.bank;

import io.roach.bank.api.Region;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "bank")
public class ApplicationModel {
    private String name = "Unnamed";

    private int defaultAccountLimit;

    private int reportQueryTimeout;

    private boolean selectForUpdate;

    private boolean clearAtStartup;

    @NotNull
    private AccountPlan accountPlan;

    @NotEmpty
    private List<Region> regions = new ArrayList<>();

    private Map<String, String> regionMapping = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClearAtStartup() {
        return clearAtStartup;
    }

    public void setClearAtStartup(boolean clearAtStartup) {
        this.clearAtStartup = clearAtStartup;
    }

    public AccountPlan getAccountPlan() {
        return accountPlan;
    }

    public void setAccountPlan(AccountPlan accountPlan) {
        this.accountPlan = accountPlan;
    }

    public int getDefaultAccountLimit() {
        return defaultAccountLimit;
    }

    public void setDefaultAccountLimit(int defaultAccountLimit) {
        this.defaultAccountLimit = defaultAccountLimit;
    }

    public int getReportQueryTimeout() {
        return reportQueryTimeout;
    }

    public void setReportQueryTimeout(int reportQueryTimeout) {
        this.reportQueryTimeout = reportQueryTimeout;
    }

    public boolean isSelectForUpdate() {
        return selectForUpdate;
    }

    public void setSelectForUpdate(boolean selectForUpdate) {
        this.selectForUpdate = selectForUpdate;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public Map<String, String> getRegionMapping() {
        return regionMapping;
    }

    public void setRegionMapping(Map<String, String> regionMapping) {
        this.regionMapping = regionMapping;
    }

    @Override
    public String toString() {
        return "ApplicationModel{" +
                "defaultAccountLimit=" + defaultAccountLimit +
                ", reportQueryTimeout=" + reportQueryTimeout +
                ", selectForUpdate=" + selectForUpdate +
                ", clearAtStartup=" + clearAtStartup +
                ", accountPlan=" + accountPlan +
                ", regions=" + regions +
                ", regionMapping=" + regionMapping +
                '}';
    }
}
