package com.dianping.pigeon.governor.service.impl;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.exception.LionNullProjectException;
import com.dianping.pigeon.governor.service.RegionRouterService;
import com.dianping.pigeon.governor.util.LionUtils;
import org.springframework.stereotype.Service;

/**
 * Created by shihuashen on 16/6/14.
 */
@Service
public class RegionRouterServiceImpl implements RegionRouterService {
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    @Override
    public boolean getEnableState(String projectName) throws LionNullProjectException{
        String lionKey = projectName+".pigeon.regions.route.enable";
        if(!LionUtils.isExistProject(projectName))
            throw new LionNullProjectException();
        if(LionUtils.isExistKey(configManager.getEnv(),lionKey)){
            String lionValue = Lion.get(lionKey);
            return Boolean.valueOf(lionValue);
        }else{
            String defaultPigeonValue = LionUtils.getKey(configManager.getEnv(),"pigeon.regions.route.enable");
            System.out.println("default pigeon value is "+defaultPigeonValue);
            LionUtils.createConfig(configManager.getEnv(),projectName,lionKey,"Region%20enable%20state");
            LionUtils.setConfig(configManager.getEnv(),lionKey,defaultPigeonValue);
            return Boolean.valueOf(defaultPigeonValue);
        }
    }

    @Override
    public boolean setEnableState(String projectName, String state) {
        String lionKey = projectName+".pigeon.regions.route.enable";
        LionUtils.setConfig(configManager.getEnv(),lionKey,state);
        return true;
    }
}
