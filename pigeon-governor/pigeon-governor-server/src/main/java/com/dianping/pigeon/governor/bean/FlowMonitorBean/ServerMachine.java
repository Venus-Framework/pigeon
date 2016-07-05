package com.dianping.pigeon.governor.bean.FlowMonitorBean;

/**
 * Created by shihuashen on 16/7/5.
 */
public class ServerMachine {
    private String ipAddress;
    public ServerMachine(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
