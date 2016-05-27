package com.dianping.pigeon.governor.bean.serviceDesc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shihuashen on 16/4/21.
 */
public class ServiceDescBean {

    private Integer serviceId;


    private String serviceName;


    private String serviceImpl;


    private Date updatetime;


    private String serviceDesc;

    private List<MethodDescBean> methodDescBeanList;


    public ServiceDescBean(){
        this.methodDescBeanList = new ArrayList<MethodDescBean>();
        this.serviceDesc="";
    }

    public void setMethodDescBeanList(List<MethodDescBean> list){
        this.methodDescBeanList.clear();
        if(list!=null)
            this.methodDescBeanList.addAll(list);
    }

    public List<MethodDescBean> getMethodDescBeanList(){
        return this.methodDescBeanList;
    }


    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }


    public String getServiceName() {
        return serviceName;
    }


    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? null : serviceName.trim();
    }


    public String getServiceImpl() {
        return serviceImpl;
    }


    public void setServiceImpl(String serviceImpl) {
        this.serviceImpl = serviceImpl == null ? null : serviceImpl.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }


    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }


    public String getServiceDesc() {
        return serviceDesc;
    }


    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc == null ? null : serviceDesc.trim();
    }
}
