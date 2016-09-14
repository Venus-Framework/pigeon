package com.dianping.pigeon.governor.bean.providerFlowLimit;

import com.dianping.pigeon.governor.exception.LionValuePraseErrorException;
import com.dianping.pigeon.governor.message.SendResult;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
        if(rawInfo.equals(""))
            return ;
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



    public List<RowData> getConfigs(){
        List<RowData> list = new LinkedList<RowData>();
        for(String requestMethod : this.methodsConfig.keySet()){
            String[] strs = requestMethod.split("#");
            String serviceName = strs[0];
            String methodName = strs[1];
            for(String appName : this.methodsConfig.get(requestMethod).keySet()){
                Long qps = this.methodsConfig.get(requestMethod).get(appName);
                list.add(new RowData(serviceName,methodName,appName,qps));
            }
        }
        return list;
    }

    private class RowData{
        private String serviceName;
        private String methodName;
        private String appName;
        private Long qps;
        public RowData(String serviceName,String methodName,String appName,Long qps){
            this.serviceName =  serviceName;
            this.methodName = methodName;
            this.appName = appName;
            this.qps = qps;
        }
        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public Long getQps() {
            return qps;
        }

        public void setQps(Long qps) {
            this.qps = qps;
        }
    }
}
