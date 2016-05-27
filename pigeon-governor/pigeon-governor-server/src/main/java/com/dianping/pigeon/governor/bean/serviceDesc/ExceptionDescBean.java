package com.dianping.pigeon.governor.bean.serviceDesc;

import com.dianping.pigeon.governor.model.ExceptionDesc;

import java.util.Date;

/**
 * Created by shihuashen on 16/4/21.
 */
public class ExceptionDescBean {
    private Integer exceptionId;


    private Integer methodId;


    private String exceptionName;


    private Date updatetime;


    private String exceptionDesc;

    public ExceptionDescBean(){
        this.exceptionDesc="";
    }

    public Integer getExceptionId() {
        return exceptionId;
    }


    public void setExceptionId(Integer exceptionId) {
        this.exceptionId = exceptionId;
    }


    public Integer getMethodId() {
        return methodId;
    }


    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }


    public String getExceptionName() {
        return exceptionName;
    }


    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName == null ? null : exceptionName.trim();
    }


    public Date getUpdatetime() {
        return updatetime;
    }


    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }


    public String getExceptionDesc() {
        return exceptionDesc;
    }


    public void setExceptionDesc(String exceptionDesc) {
        this.exceptionDesc = exceptionDesc == null ? null : exceptionDesc.trim();
    }
}
