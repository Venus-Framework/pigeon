package com.dianping.pigeon.governor.monitor.loadBalanceMonitor.skewMessage;

import java.util.Map;

/**
 * Created by shihuashen on 16/7/13.
 */
public class ClientSkewMessage {
    private String serverProjectName;
    private String clientProjectName;
    private Map<String,Long> flowDistributed;

    public String getServerProjectName() {
        return serverProjectName;
    }

    public void setServerProjectName(String serverProjectName) {
        this.serverProjectName = serverProjectName;
    }

    public String getClientProjectName() {
        return clientProjectName;
    }

    public void setClientProjectName(String clientProjectName) {
        this.clientProjectName = clientProjectName;
    }

    public Map<String, Long> getFlowDistributed() {
        return flowDistributed;
    }

    public void setFlowDistributed(Map<String, Long> flowDistributed) {
        this.flowDistributed = flowDistributed;
    }
}
