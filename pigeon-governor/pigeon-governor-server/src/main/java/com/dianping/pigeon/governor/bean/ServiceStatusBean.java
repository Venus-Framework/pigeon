package com.dianping.pigeon.governor.bean;

/**
 * Created by chenchongze on 15/10/30.
 */
public class ServiceStatusBean {

    private String status = "";

    private String size = "";

    private String version = "";

    private String env = "";

    private String group = "";

    private String app = "";

    private String published = "";

    private String online = "";

    private String error = "";

    private String registry = "";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    @Override
    public String toString() {
        return "ServiceStatusBean{" +
                "status='" + status + '\'' +
                ", size='" + size + '\'' +
                ", version='" + version + '\'' +
                ", env='" + env + '\'' +
                ", group='" + group + '\'' +
                ", app='" + app + '\'' +
                ", published='" + published + '\'' +
                ", online='" + online + '\'' +
                ", error='" + error + '\'' +
                ", registry='" + registry + '\'' +
                '}';
    }
}
