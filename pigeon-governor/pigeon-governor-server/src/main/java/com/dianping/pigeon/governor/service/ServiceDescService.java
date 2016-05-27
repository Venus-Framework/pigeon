package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.scanServiceDesc.UpdateResultState;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;

import java.util.Map;

/**
 * Created by shihuashen on 16/4/22.
 */
public interface ServiceDescService {
    ServiceDescBean getServiceDescBeanById(Integer serviceId);
    void updateServiceDescById(Integer serviceId, String desc);
    void updateMethodDescById(Integer methodId, String value);
    void updateParamDescById(Integer paramId, String value);
    void updateExceptionDescById(Integer exceptionId, String value);
    Map<String, Object> getServiceMetaInfoById(Integer serviceId);
    UpdateResultState updateServiceDescBean(ServiceDescBean serviceDescBean);
    void removeServiceDesc(Integer serviceId);
}
