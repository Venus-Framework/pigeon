package com.dianping.pigeon.governor.bean.providerFlowLimit;

import com.dianping.pigeon.governor.exception.LionValuePraseErrorException;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by shihuashen on 16/9/9.
 */
public class MethodAppLimit {
    Logger logger = LogManager.getLogger();
    private Map<String,Map<String,Long>> methodsConfig = new HashMap<String,Map<String, Long>>();
    public MethodAppLimit(){
        this.methodsConfig = new HashMap<String, Map<String, Long>>();
    }
    public MethodAppLimit(String rawInfo) throws LionValuePraseErrorException {
        this();
        try{
            this.methodsConfig = GsonUtils.fromJson(rawInfo,this.methodsConfig.getClass());
        }catch (Throwable t){
            logger.error(t);
            throw new LionValuePraseErrorException();
        }

    }
    public boolean add(String serviceName,String methodName,String appName,Long value){
        String requestMethod = serviceName+"#"+methodName;
        if(this.methodsConfig.containsKey(requestMethod)){
            if(this.methodsConfig.get(requestMethod).containsKey(appName))
                return false;
            this.methodsConfig.get(requestMethod).put(appName,value);
            return true;
        }else{
            Map<String,Long> appConfigs = new HashMap<String, Long>();
            appConfigs.put(appName,value);
            this.methodsConfig.put(requestMethod,appConfigs);
            return true;
        }
    }
    //check the value is equals or not
    public boolean remove(String serviceName,String methodName,String appName,Long value){
        String requestMethod = serviceName+"#"+methodName;
        if(this.methodsConfig.containsKey(requestMethod)&&this.methodsConfig.get(requestMethod).containsKey(appName)){
            this.methodsConfig.get(requestMethod).remove(appName);
            return true;
        }else
            return false;
    }
    public boolean update(String serviceName,String methodName,String appName,Long value){
        String requestMethod = serviceName+"#"+methodName;
        if(this.methodsConfig.containsKey(requestMethod)&&this.methodsConfig.get(requestMethod).containsKey(appName)){
            this.methodsConfig.get(requestMethod).put(appName,value);
            return true;
        }else return false;
    }
    public String toString(){
        return GsonUtils.toJson(this.methodsConfig);
    }



}
