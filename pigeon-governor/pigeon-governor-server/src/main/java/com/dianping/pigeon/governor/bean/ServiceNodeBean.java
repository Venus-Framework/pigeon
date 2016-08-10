package com.dianping.pigeon.governor.bean;

import com.dianping.pigeon.governor.model.ServiceNode;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by chenchongze on 16/8/2.
 */
public class ServiceNodeBean {

    private String serviceName;
    private String group;
    private String ip;
    private String port;
    private String projectName;

    public ServiceNodeBean() {
    }

    public ServiceNodeBean(String serviceName, String group, String ip, String port, String projectName) {
        this.serviceName = serviceName;
        this.group = group;
        this.ip = ip;
        this.port = port;
        this.projectName = projectName;
    }

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceNodeBean that = (ServiceNodeBean) o;

        if (!group.equals(that.group)) return false;
        if (!ip.equals(that.ip)) return false;
        if (!port.equals(that.port)) return false;
        if (!projectName.equals(that.projectName)) return false;
        if (!serviceName.equals(that.serviceName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + group.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + projectName.hashCode();
        return result;
    }

    public ServiceNode newServiceNode() {
        ServiceNode serviceNode = new ServiceNode();
        serviceNode.setServiceName(serviceName);
        serviceNode.setGroup(group);
        serviceNode.setIp(ip);
        serviceNode.setPort(port);
        serviceNode.setProjectName(projectName);

        return serviceNode;
    }

    public static void main(String[] args) {
        ServiceNodeBean serviceNodeBean1 = new ServiceNodeBean("com.dianping.pigeon.governor.service.RegistrationInfoService",
                "ccz", "172.24.123.146",  "4297", "pigeon-governor-server");
        ServiceNodeBean serviceNodeBean2 = new ServiceNodeBean("com.dianping.pigeon.governor.service.RegistrationInfoService",
                "", "192.168.225.149",  "4297", "pigeon-governor-server");
        Set<ServiceNodeBean> serviceNodeBeanSet = Sets.newHashSet();
        serviceNodeBeanSet.add(serviceNodeBean1);

        System.out.println(serviceNodeBeanSet.contains(serviceNodeBean2));
    }
}
