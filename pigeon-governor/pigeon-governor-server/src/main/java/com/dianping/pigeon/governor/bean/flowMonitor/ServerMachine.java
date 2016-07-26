package com.dianping.pigeon.governor.bean.flowMonitor;

/**
 * Created by shihuashen on 16/7/5.
 */
public class ServerMachine {
    private String projectName;
    private String date;
    private String ipAddress;

    public ServerMachine(String projectName, String date, String ipAddress) {
        this.projectName = projectName;
        this.date = date;
        this.ipAddress = ipAddress;
    }

    public ServerMachine(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
