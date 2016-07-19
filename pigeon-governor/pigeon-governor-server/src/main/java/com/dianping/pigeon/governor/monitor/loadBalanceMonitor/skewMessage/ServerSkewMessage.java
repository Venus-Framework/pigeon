package com.dianping.pigeon.governor.monitor.loadBalanceMonitor.skewMessage;

import java.util.List;
import java.util.Map;

/**
 * Created by shihuashen on 16/7/13.
 */
public class ServerSkewMessage {
    private String projectName;
    private Map<String,Long> flowDistributed;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, Long> getFlowDistributed() {
        return flowDistributed;
    }

    public void setFlowDistributed(Map<String, Long> flowDistributed) {
        this.flowDistributed = flowDistributed;
    }
}
