package com.dianping.pigeon.registry.mns.mock;

/**
 * Created by chenchongze on 16/6/3.
 */
public class SGService {

    /**
     * unique key: appkey#serviceName#ip:port
     */

    private String appkey; // app.name
    private String serviceName; // serviceName
    private String version; // client version
    private String ip; // required
    private int port; // required
    private int weight; // required
    private int status; // required
    private int role;
    private int envir; // appenv
    private int lastUpdateTime;
    private String extend;
    private double fweight;
    private int serverType; // required
    private int protocolType; // 0:false, 1:true

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getEnvir() {
        return envir;
    }

    public void setEnvir(int envir) {
        this.envir = envir;
    }

    public int getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(int lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public double getFweight() {
        return fweight;
    }

    public void setFweight(double fweight) {
        this.fweight = fweight;
    }

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SGService sgService = (SGService) o;

        if (port != sgService.port) return false;
        if (!appkey.equals(sgService.appkey)) return false;
        if (!ip.equals(sgService.ip)) return false;
        if (!serviceName.equals(sgService.serviceName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appkey.hashCode();
        result = 31 * result + serviceName.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + port;
        return result;
    }
}
