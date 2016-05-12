package com.dianping.pigeon.governor.bean.serviceDesc;

import java.util.Date;

/**
 * Created by shihuashen on 16/4/21.
 */
public class ParamDescBean {
    private Integer paramId;
    private Integer methodId;
    private String paramName;
    private Date updatetime;
    private String paramDesc;
    public ParamDescBean(){
        this.paramDesc = "";
    }
    public Integer getParamId() {
        return paramId;
    }
    public void setParamId(Integer paramId) {
        this.paramId = paramId;
    }
    public Integer getMethodId() {
        return methodId;
    }
    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }
    public String getParamName() {
        return paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName == null ? null : paramName.trim();
    }
    public Date getUpdatetime() {
        return updatetime;
    }
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
    public String getParamDesc() {
        return paramDesc;
    }
    public void setParamDesc(String paramDesc) {
        this.paramDesc = paramDesc == null ? null : paramDesc.trim();
    }
}
