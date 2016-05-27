package com.dianping.pigeon.governor.bean.serviceDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by shihuashen on 16/4/21.
 */
public class MethodDescBean {

    private Integer methodId;


    private Integer serviceId;


    private String methodName;


    private String methodFullname;


    private String methodReturnType;


    private String methodDesc;

    private String methodReturnDesc;


    private Date updatetime;

    private List<ParamDescBean> paramDescBeanArrayList;

    private List<ExceptionDescBean> exceptionDescBeanArrayList;


    public void setMethodDesc(String methodDesc){
        this.methodDesc = methodDesc;
    }
    public String getMethodDesc(){
        return this.methodDesc;
    }

    public void setMethodReturnDesc(String methodReturnDesc){
        this.methodReturnDesc = methodReturnDesc;
    }

    public String getMethodReturnDesc(){
        return this.methodReturnDesc;
    }

    public MethodDescBean(){
        this.paramDescBeanArrayList = new ArrayList<ParamDescBean>();
        this.exceptionDescBeanArrayList = new ArrayList<ExceptionDescBean>();
        this.methodDesc="";
        this.methodReturnDesc="";
    }

    public void setParamDescBeanArrayList(List<ParamDescBean> list){
        this.paramDescBeanArrayList.clear();
        if(list!=null)
            this.paramDescBeanArrayList.addAll(list);
    }

    public List<ParamDescBean> getParamDescBeanArrayList(){
        return this.paramDescBeanArrayList;
    }

    public void setExceptionDescBeanArrayList(List<ExceptionDescBean> list){
        this.exceptionDescBeanArrayList.clear();
        if(list!=null)
            this.exceptionDescBeanArrayList.addAll(list);
    }

    public List<ExceptionDescBean> getExceptionDescBeanArrayList(){
        return this.exceptionDescBeanArrayList;
    }

    public Integer getMethodId() {
        return methodId;
    }


    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }


    public Integer getServiceId() {
        return serviceId;
    }


    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getMethodName() {
        return methodName;
    }


    public void setMethodName(String methodName) {
        this.methodName = methodName == null ? null : methodName.trim();
    }


    public String getMethodFullname() {
        return methodFullname;
    }


    public void setMethodFullname(String methodFullname) {
        this.methodFullname = methodFullname == null ? null : methodFullname.trim();
    }


    public String getMethodReturnType() {
        return methodReturnType;
    }


    public void setMethodReturnType(String methodReturnType) {
        this.methodReturnType = methodReturnType == null ? null : methodReturnType.trim();
    }


    public Date getUpdatetime() {
        return updatetime;
    }


    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}
