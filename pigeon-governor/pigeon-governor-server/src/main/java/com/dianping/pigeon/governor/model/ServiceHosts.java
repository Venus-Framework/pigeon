package com.dianping.pigeon.governor.model;

/**
 * Created by shihuashen on 16/4/27.
 */
public class ServiceHosts {
    private Integer serviceId;
    private String serviceName;
    private String hosts;
    private String group;

    public Integer getServiceId(){
        return serviceId;
    }
    public void setServiceId(Integer id){
        this.serviceId = id;
    }
    public String getServiceName(){
        return serviceName;
    }
    public void setServiceName(String name){
        this.serviceName = name;
    }
    public String getHosts(){
        return this.hosts;
    }
    public void setHosts(String hosts){
        this.hosts = hosts;
    }
    public String getGroup(){
        return this.group;
    }
    public void setGroup(String group){
        this.group = group;
    }
}
