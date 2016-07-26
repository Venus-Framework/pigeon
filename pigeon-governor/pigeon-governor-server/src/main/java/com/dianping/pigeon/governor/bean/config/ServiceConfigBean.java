package com.dianping.pigeon.governor.bean.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/5/19.
 */
public class ServiceConfigBean {
    private String serviceName;
    private HashMap<String,RouterInfo> invokerConfigs;
    private HashMap<String,RouterInfo> providerConfigs;
    public ServiceConfigBean(){
        this.invokerConfigs = new HashMap<String, RouterInfo>();
        this.providerConfigs = new HashMap<String, RouterInfo>();
    }
    public void setInvokerConfigs(HashMap<String,RouterInfo> map){
        this.invokerConfigs.clear();
        this.invokerConfigs.putAll(map);
    }
    public void setProviderConfigs(HashMap<String,RouterInfo> map){
        this.providerConfigs.clear();
        this.providerConfigs.putAll(map);
    }
    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
    public String getServiceName(){
        return this.serviceName;
    }
    public List<RouterInfo> getInvokerRouterInfo(){
        List<RouterInfo> list = new LinkedList<RouterInfo>();
        list.addAll(this.invokerConfigs.values());
        return list;
    }
    public List<RouterInfo> getProviderRouterInfo(){
        List<RouterInfo> list = new LinkedList<RouterInfo>();
        list.addAll(this.providerConfigs.values());
        return list;
    }
}
