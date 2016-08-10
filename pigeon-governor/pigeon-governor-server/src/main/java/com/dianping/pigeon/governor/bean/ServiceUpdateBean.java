package com.dianping.pigeon.governor.bean;

import java.util.List;

/**
 * Created by chenchongze on 16/7/29.
 */
public class ServiceUpdateBean {

    private String serviceName;
    private String group;
    private List<String> toAddHosts;
    private List<String> toDelHosts;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getToAddHosts() {
        return toAddHosts;
    }

    public void setToAddHosts(List<String> toAddHosts) {
        this.toAddHosts = toAddHosts;
    }

    public List<String> getToDelHosts() {
        return toDelHosts;
    }

    public void setToDelHosts(List<String> toDelHosts) {
        this.toDelHosts = toDelHosts;
    }
}
