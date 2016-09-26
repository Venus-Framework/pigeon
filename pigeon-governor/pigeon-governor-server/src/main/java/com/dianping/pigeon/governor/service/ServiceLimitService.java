package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.providerFlowLimit.AppLimit;
import com.dianping.pigeon.governor.bean.providerFlowLimit.MethodAppLimit;
import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.exception.LionValuePraseErrorException;

/**
 * Created by shihuashen on 16/9/9.
 */
public interface ServiceLimitService {
    boolean getAppLimitState(String projectName) throws LionNullProjectException;
    boolean getMethodLimitState(String projectName) throws LionNullProjectException;
    boolean setAppLimitState(String projectName,boolean state);
    boolean setMethodAppLimit(String projectName,boolean state);
    AppLimit getAppLimit(String projectName) throws LionNullProjectException, LionValuePraseErrorException;
    boolean addAppLimit(String projectName,String appName,Long qpsLimitation) throws LionValuePraseErrorException;
    boolean removeAppLimit(String projectName,String appName,Long qpsLimitation) throws LionValuePraseErrorException;
    boolean updateAppLimit(String projectName,String appName,Long qpsLimitation) throws LionValuePraseErrorException;
    MethodAppLimit getMethodAppLimit(String projectName) throws LionValuePraseErrorException, LionNullProjectException;
    boolean addMethodAppLimit(String projectName,String serviceName,String methodName,String appName,Long value) throws LionValuePraseErrorException;
    boolean removeMethodAppLimit(String projectName,String serviceName,String methodName,String appName,Long value) throws LionValuePraseErrorException;
    boolean updateMethodAppLimit(String projectName,String serviceName,String methodName,String appName,Long value) throws LionValuePraseErrorException;
}
