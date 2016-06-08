package com.dianping.pigeon.governor.bean.ConfigBean;

/**
 * Created by shihuashen on 16/5/19.
 */
public class RouterInfo {
    private String ipAddress;
    private String group;

    public RouterInfo(String s1,String s2){
        this.ipAddress = s1;
        this.group = s2;
    }
    public String getIpAddress(){
        return ipAddress;
    }
    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }
    public String getGroup(){
        return group;
    }
    public void setGroup(String group){
        this.group = group;
    }
}
