package com.dianping.pigeon.governor.bean.scanServiceDesc;

import java.util.List;

/**
 * Created by shihuashen on 16/5/4.
 */
public class JsonHostInfo {
    private String port;
    private String pigeonVersion;
    private String env;
    private String group;
    private String app;
    private boolean published;
    private boolean online;
    private List<JsonWeight> weights;
    private List<JsonService> services;
    public String getGroup(){
        return this.group;
    }
    public List<JsonService> getServices(){
        return this.services;
    }
}
