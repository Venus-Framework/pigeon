package com.dianping.pigeon.governor.service.impl;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.bean.providerFlowLimit.AppLimit;
import com.dianping.pigeon.governor.bean.providerFlowLimit.MethodAppLimit;
import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.exception.LionValuePraseErrorException;
import com.dianping.pigeon.governor.service.ServiceLimitService;
import com.dianping.pigeon.governor.util.LionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Created by shihuashen on 16/9/12.
 */
@Service
public class ServiceLimitServiceImpl implements ServiceLimitService {
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private Logger logger = LogManager.getLogger(InvokerDegradeServiceImpl.class.getName());
    private static String APP_LIMIT_TRIE = ".pigeon.provider.applimit";
    private static String METHOD_LIMIT_TRIE =".pigeon.provider.methodapplimit";
    private static String APP_LIMIT_STATE_TRIE = ".pigeon.provider.applimit.enable";
    private static String METHOD_APP_LIMIT_STATE_TRIE = ".pigeon.provider.methodapplimit.enable";
    private static boolean defaultAppLimitState = false;
    private static boolean defaultMethodAppLimitState = false;
    @Override
    public boolean getAppLimitState(String projectName) throws LionNullProjectException {
        String lionKey = projectName+APP_LIMIT_STATE_TRIE;
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Boolean.valueOf(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"APP%20limit%20%enable%20state");
            LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(defaultAppLimitState));
            return Boolean.valueOf(defaultAppLimitState);
        }
    }

    @Override
    public boolean getMethodLimitState(String projectName) throws LionNullProjectException {
        String lionKey = projectName+METHOD_APP_LIMIT_STATE_TRIE;
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Boolean.valueOf(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"Method%20APP%20limit%20%enable%20state");
            LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(defaultMethodAppLimitState));
            return Boolean.valueOf(defaultMethodAppLimitState);
        }
    }

    @Override
    public boolean setAppLimitState(String projectName, boolean state) {
        String lionKey = projectName+APP_LIMIT_STATE_TRIE;
        return LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(state));
    }

    @Override
    public boolean setMethodAppLimit(String projectName, boolean state) {
        String lionKey = projectName+METHOD_APP_LIMIT_STATE_TRIE;
        return LionUtils.setConfig(configManager.getEnv(),lionKey,String.valueOf(state));
    }

    @Override
    public AppLimit getAppLimit(String projectName) throws LionNullProjectException, LionValuePraseErrorException {
        String lionKey = projectName+APP_LIMIT_TRIE;
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return new AppLimit(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"app%20limit");
            LionUtils.setConfig(configManager.getEnv(),lionKey,"");
            return new AppLimit();
        }
    }

    @Override
    public boolean addAppLimit(String projectName, String appName, Long qpsLimitation) throws LionValuePraseErrorException {
        String lionKey = projectName+APP_LIMIT_TRIE;
        AppLimit appLimit = new AppLimit(configManager.getStringValue(lionKey,""));
        if(appLimit.add(appName,qpsLimitation))
            return LionUtils.setConfig(configManager.getEnv(),lionKey,appLimit.toString());
        return false;
    }

    @Override
    public boolean removeAppLimit(String projectName, String appName, Long qpsLimitation) throws LionValuePraseErrorException {
        String lionKey = projectName+APP_LIMIT_TRIE;
        AppLimit appLimit = new AppLimit(configManager.getStringValue(lionKey,""));
        if(appLimit.remove(appName,qpsLimitation))
            return LionUtils.setConfig(configManager.getEnv(),lionKey,appLimit.toString());
        return false;
    }

    @Override
    public boolean updateAppLimit(String projectName, String appName, Long qpsLimitation) throws LionValuePraseErrorException {
        String lionKey = projectName+APP_LIMIT_TRIE;
        AppLimit appLimit = new AppLimit(configManager.getStringValue(lionKey,""));
        if(appLimit.update(appName,qpsLimitation))
            return LionUtils.setConfig(configManager.getEnv(),lionKey,appLimit.toString());
        return false;
    }

    @Override
    public MethodAppLimit getMethodAppLimit(String projectName) throws LionValuePraseErrorException, LionNullProjectException {
        String lionKey = projectName+METHOD_LIMIT_TRIE;
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return new MethodAppLimit(lionValue);
        }else{
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"method%20app%20limit");
            LionUtils.setConfig(configManager.getEnv(),lionKey,"");
            return new MethodAppLimit();
        }
    }

    @Override
    public boolean addMethodAppLimit(String projectName, String serviceName, String methodName, String appName, Long value) throws LionValuePraseErrorException {
        String lionKey = projectName+METHOD_LIMIT_TRIE;
        MethodAppLimit methodAppLimit = new MethodAppLimit(configManager.getStringValue(lionKey,""));
        if(methodAppLimit.add(serviceName,methodName,appName,value))
            return LionUtils.setConfig(configManager.getEnv(),lionKey,methodAppLimit.toString());
        return false;
    }

    @Override
    public boolean removeMethodAppLimit(String projectName, String serviceName, String methodName, String appName, Long value) throws LionValuePraseErrorException {
        String lionKey = projectName+METHOD_LIMIT_TRIE;
        MethodAppLimit methodAppLimit = new MethodAppLimit(configManager.getStringValue(lionKey,""));
        if(methodAppLimit.remove(serviceName,methodName,appName,value))
            return LionUtils.setConfig(configManager.getEnv(),lionKey,methodAppLimit.toString());
        return false;
    }

    @Override
    public boolean updateMethodAppLimit(String projectName, String serviceName, String methodName, String appName, Long value) throws LionValuePraseErrorException {
        String lionKey = projectName+METHOD_LIMIT_TRIE;
        MethodAppLimit methodAppLimit = new MethodAppLimit(configManager.getStringValue(lionKey,""));
        if(methodAppLimit.update(serviceName,methodName,appName,value))
            return LionUtils.setConfig(configManager.getEnv(),lionKey,methodName.toString());
        return false;
    }
}
