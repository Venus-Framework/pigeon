package com.dianping.pigeon.console.domain;

/**
 * Created by chenchongze on 16/6/12.
 */
public class RegionStatus {

    Boolean regionPolicyEnabled = Boolean.FALSE; // pigeon.regions.enable
    String regionInfos = "none"; // pigeon.regions
    String localRegion = "none";
    String regionPrefer = "none";

    //TODO 各个服务的region策略


    public Boolean getRegionPolicyEnabled() {
        return regionPolicyEnabled;
    }

    public void setRegionPolicyEnabled(Boolean regionPolicyEnabled) {
        this.regionPolicyEnabled = regionPolicyEnabled;
    }

    public String getRegionInfos() {
        return regionInfos;
    }

    public void setRegionInfos(String regionInfos) {
        this.regionInfos = regionInfos;
    }

    public String getLocalRegion() {
        return localRegion;
    }

    public void setLocalRegion(String localRegion) {
        this.localRegion = localRegion;
    }

    public String getRegionPrefer() {
        return regionPrefer;
    }

    public void setRegionPrefer(String regionPrefer) {
        this.regionPrefer = regionPrefer;
    }

}
