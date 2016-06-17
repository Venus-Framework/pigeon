package com.dianping.pigeon.registry.mns.mock;

/**
 * Created by chenchongze on 16/6/3.
 */
public class ServiceListRequest {

    private String appkey; // app.name
    private String serviceName; // serviceName
    private String ip; // required
    private int port; // required

    public String getServiceId() {
        return appkey + "#" + serviceName;
    }

    public String getHostId() {
        return ip + "#" + port;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
