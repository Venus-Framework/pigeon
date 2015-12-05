package com.dianping.pigeon.governor.bean;

/**
 * Created by chenchongze on 15/11/27.
 */
public class ConsoleServiceJsonBean {

    private String port;
    private String pigeonVersion;
    private String env;
    private String group;
    private String app;
    private String published;
    private String online;
    private Object[] weights;
    private ConsoleService[] services;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPigeonVersion() {
        return pigeonVersion;
    }

    public void setPigeonVersion(String pigeonVersion) {
        this.pigeonVersion = pigeonVersion;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public Object[] getWeights() {
        return weights;
    }

    public void setWeights(Object[] weights) {
        this.weights = weights;
    }

    public ConsoleService[] getServices() {
        return services;
    }

    public void setServices(ConsoleService[] services) {
        this.services = services;
    }
}
