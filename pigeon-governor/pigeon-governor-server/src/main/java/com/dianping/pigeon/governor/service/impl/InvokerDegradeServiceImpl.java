package com.dianping.pigeon.governor.service.impl;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.bean.degrade.DegradeConfig;
import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.service.InvokerDegradeService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.LionUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.net.URLEncoder;

/**
 * Created by shihuashen on 16/8/17.
 */
@Service
public class InvokerDegradeServiceImpl implements InvokerDegradeService{
    private boolean defaultForceDegradeState =  false;
    private boolean defaultAutoDegradeState = false;
    private boolean defaultFailureDegradeState = false;
    private double defaultRecoverPercentage = 1;
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    @Override
    public boolean getForceDegradeState(String projectName) throws LionNullProjectException {
        String lionKey = projectName+".pigeon.invoker.degrade.force";
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Boolean.valueOf(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"Force%20enable%20degrade");
            LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(defaultForceDegradeState));
            return Boolean.valueOf(defaultForceDegradeState);
        }
    }


    @Override
    public boolean setForceDegradeState(String projectName, String state) {
        String lionKey = projectName+".pigeon.invoker.degrade.force";
        return LionUtils.setConfig(configManager.getEnv(),lionKey,state);
    }

    @Override
    public boolean getAutoDegradeState(String projectName) throws LionNullProjectException {
        String lionKey = projectName+".pigeon.invoker.degrade.auto";
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Boolean.valueOf(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"Auto%20enable%20degrade");
            LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(defaultAutoDegradeState));
            return Boolean.valueOf(defaultAutoDegradeState);
        }
    }

    @Override
    public boolean setAutoDegradeState(String projectName, String state) {
        System.out.println(projectName);
        String lionkey = projectName+".pigeon.invoker.degrade.auto";
        return LionUtils.setConfig(configManager.getEnv(),lionkey,state);
    }

    @Override
    public boolean getFailureDegradeState(String projectName) throws LionNullProjectException {
        String lionKey = projectName+".pigeon.invoker.degrade.failure";
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Boolean.valueOf(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"Failure%20enable%20degrade");
            LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(defaultFailureDegradeState));
            return Boolean.valueOf(defaultFailureDegradeState);
        }
    }

    @Override
    public boolean setFailureDegradeState(String projectName, String state) {
        String lionKey = projectName+".pigeon.invoker.degrade.failure";
        return LionUtils.setConfig(configManager.getEnv(),lionKey,state);
    }


    //TODO check not to add the same config.
    @Override
    public boolean addDegradeConfig(DegradeConfig config)   {
        String projectName = config.getProjectName();
        Map<String,String> map = new HashMap<String,String>();
        try{
            map = getRawMethodsInfo(config.getProjectName());
        } catch (LionNullProjectException e) {
            e.printStackTrace();
            return false;
        }catch(Throwable t){
            t.printStackTrace();
            return false;
        }
        GsonUtils.Print(map);
        String index = config.getServiceName()+"#"+config.getMethodName();
        if(map.containsKey(index))
            return false;
        else{
            Set<String> values = new HashSet<String>();
            values.addAll(map.values());
            String generateIndex = null;
            for(int i = 0;;i++){
                String tmp = "value"+i;
                if(!values.contains(tmp)){
                    generateIndex = tmp;
                    break;
                }
            }
            map.put(index,generateIndex);
            String methodsConfigKey = projectName+".pigeon.invoker.degrade.methods";
            String methodsConfigValue = null;
            String methodConfigKey = projectName+".pigeon.invoker.degrade.method.return."+generateIndex;
            String returnValue = null;
            try {
                methodsConfigValue = URLEncoder.encode(constructRawMethodsInfo(map),"UTF-8");
                returnValue =  URLEncoder.encode(config.getReturnValue(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            if(LionUtils.setConfig(configManager.getEnv(),methodsConfigKey,methodsConfigValue)){
                LionUtils.createConfig(configManager.getEnv(),projectName,methodConfigKey,"Frontend%Auto%20generate");
                return LionUtils.setConfig(configManager.getEnv(),methodConfigKey,returnValue);
            }
        }
        return false;
    }

    @Override
    public boolean updateDegradeConfig(DegradeConfig config) {
        String projectName = config.getProjectName();
        Map<String,String> map = new HashMap<String,String>();
        try {
            map = getRawMethodsInfo(projectName);
        } catch (LionNullProjectException e) {
            e.printStackTrace();
            return false;
        }catch(Throwable t){
            t.printStackTrace();
            return false;
        }
        String configPrefix = config.getServiceName()+"#"+config.getMethodName();
        if(map.containsKey(configPrefix)){
            String index = map.get(configPrefix);
            String returnConfigKey = projectName+".pigeon.invoker.degrade.method.return."+index;
            String returnConfigValue = null;
            try{
                returnConfigValue = URLEncoder.encode(config.getReturnValue(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            return LionUtils.setConfig(configManager.getEnv(),returnConfigKey,returnConfigValue);
        }else
            return false;
    }

    @Override
    public boolean deleteDegradeConfig(DegradeConfig config) {
        String projectName = config.getProjectName();
        Map<String,String> map = new HashMap<String, String>();
        try {
            map = getRawMethodsInfo(projectName);
        } catch (LionNullProjectException e) {
            e.printStackTrace();
        }
        String key = config.getServiceName()+"#"+config.getMethodName();
        if(map.containsKey(key)){
            String index = map.get(key);
            map.remove(key);
            String rawMethodsConfigValue = constructRawMethodsInfo(map);
            String methodsConfigKey = projectName+".pigeon.invoker.degrade.methods";
            String methodsConfigValue = null;
            try {
                methodsConfigValue = URLEncoder.encode(rawMethodsConfigValue,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            if(LionUtils.setConfig(configManager.getEnv(),methodsConfigKey,methodsConfigValue)){
                 return LionUtils.setConfig(configManager.getEnv(),projectName+".pigeon.invoker.degrade.method.return."+index,"");
            }

        }
        return false;
    }

    @Override
    public List<DegradeConfig> getDegradeConfigs(String projectName) {
        List<DegradeConfig> degradeConfigs = new LinkedList<DegradeConfig>();
        Map<String,String> rawMap = new HashMap<String, String>();
        try {
           rawMap = getRawMethodsInfo(projectName);
        } catch (LionNullProjectException e) {
            e.printStackTrace();
        }
        for(Iterator<String> iterator = rawMap.keySet().iterator();
                iterator.hasNext();){
            String index = iterator.next();
            String suffix = rawMap.get(index);
            int splitIndex = index.indexOf('#');
            if(splitIndex<0){
                break;
            }else{
                String serviceName=index.substring(0,splitIndex);
                String methodName=index.substring(splitIndex+1,index.length());
                String methodConfigKey = projectName+".pigeon.invoker.degrade.method.return."+suffix;
                String returnValue = Lion.get(methodConfigKey);
                DegradeConfig config = new DegradeConfig();
                config.setProjectName(projectName);
                config.setServiceName(serviceName);
                config.setMethodName(methodName);
                config.setReturnValue(returnValue);
                config.setReturnPattern(returnValueAnalysis(returnValue));
                degradeConfigs.add(config);
            }
        }
        return degradeConfigs;
    }

    @Override
    public double getRecoverPercentage(String projectName) throws LionNullProjectException {
        String lionKey = projectName+".pigeon.invoker.degrade.recover.percent";
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Double.valueOf(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"Degrade%20revover%20percentage");
            LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(defaultRecoverPercentage));
            return Double.valueOf(defaultRecoverPercentage);
        }
    }

    @Override
    public boolean setRecoverPercentage(String projectName, double value) {
        String lionKey = projectName+".pigeon.invoker.degrade.recover.percent";
        return LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(value));
    }


    private Map<String,String> getRawMethodsInfo(String projectName) throws LionNullProjectException {
        String rawString = "";
        String lionKey = projectName+".pigeon.invoker.degrade.methods";
        Map<String,String> map = new HashMap<String, String>();
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            rawString = Lion.get(lionKey);
            if(rawString==null)
                return map;
            String[] strs = rawString.split(",");
            GsonUtils.Print(strs);
            for(int i = 0;i<strs.length;i++){
                String tmp = strs[i];
                int index = tmp.indexOf('=');
                if(index<0)
                    break;
                String key = tmp.substring(0,tmp.indexOf('='));
                String value = tmp.substring(tmp.indexOf('=')+1,tmp.length());
                map.put(key,value);
            }
            return map;
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"degrades%20methods%20raw%20info");
            return map;
        }
    }

    private String constructRawMethodsInfo(Map<String,String> map){
        StringBuilder sb = new StringBuilder();
        if(map.isEmpty())
            return "";
        for(Iterator<String> iterator = map.keySet().iterator();
                iterator.hasNext();){
            String key = iterator.next();
            String value = map.get(key);
            sb.append(key+"="+value+",");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


    //TODO better define.
    private int returnValueAnalysis(String returnValue){
        if(returnValue==null||returnValue.equals("null")||returnValue.equals("")){
            return 0;
        }
        if(returnValue.contains("returnClass")){
            return 1;
        }
        if(returnValue.contains("throwException")){
            return 2;
        }
        if(returnValue.equals("{\"useMockClass\":\"true\"}"))
            return 3;
        return 0;
    }


}
