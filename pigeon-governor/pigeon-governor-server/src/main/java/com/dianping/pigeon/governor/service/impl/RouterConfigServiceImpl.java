package com.dianping.pigeon.governor.service.impl;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.bean.config.ProjectConfigBean;
import com.dianping.pigeon.governor.bean.config.RouterInfo;
import com.dianping.pigeon.governor.bean.config.ServiceConfigBean;
import com.dianping.pigeon.governor.dao.ProjectConfigMapper;
import com.dianping.pigeon.governor.service.RouterConfigService;
import com.dianping.pigeon.governor.util.HttpCallUtils;
import com.dianping.pigeon.governor.util.LionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;

/**
 * Created by shihuashen on 16/5/24.
 */
@Service
public class RouterConfigServiceImpl implements RouterConfigService {
    @Autowired
    private ProjectConfigMapper projectConfigMapper;


    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    @Override
    public ProjectConfigBean getProjectConfig(String projectName) {
        if(projectConfigMapper.getProjectCount(projectName)==0)
            return null;
        List<String> serviceNames = projectConfigMapper.getServiceNames(projectName);
        List<String> filteredServiceNames = new ArrayList<String>();
        Iterator<String> iterator = serviceNames.iterator();
        while(iterator.hasNext()){
            String serviceName = iterator.next();
            if(!filteredServiceNames.contains(serviceName))
                filteredServiceNames.add(serviceName);
        }
        ProjectConfigBean projectConfigBean = new ProjectConfigBean();
        projectConfigBean.setProjectName(projectName);
        Map<String,List<RouterInfo>> invokers =  LionUtils.getServiceRouterConfigsPerProject(projectName,"invoker",configManager.getEnv());
        Map<String,List<RouterInfo>> providers =  LionUtils.getServiceRouterConfigsPerProject(projectName,"provider",configManager.getEnv());
        List<ServiceConfigBean> serviceConfigBeans = new ArrayList<ServiceConfigBean>();
        iterator = filteredServiceNames.iterator();
        while(iterator.hasNext()){
            String serviceName = iterator.next();
            ServiceConfigBean serviceConfigBean = new ServiceConfigBean();
            serviceConfigBean.setServiceName(serviceName);
            if(!invokers.isEmpty())
                serviceConfigBean.setInvokerConfigs(parseConfigsMap(invokers.get(serviceName)));
            if(!providers.isEmpty())
                serviceConfigBean.setProviderConfigs(parseConfigsMap(providers.get(serviceName)));
            serviceConfigBeans.add(serviceConfigBean);
        }
        projectConfigBean.setServices(serviceConfigBeans);
        return projectConfigBean;
    }

    @Override
    public boolean deleteRouterConfig(String projectName, String serviceName, String type, String ipAddress, String group) {
        String lionKey = projectName+".pigeon.group."+type+"."+ipAddress;
        String value = Lion.getStringValue(lionKey,"");
        System.out.println(value);
        Map<String,String> map = LionUtils.convertRawLionConfigValue(value);
        if(map.containsKey(serviceName)){
            if(map.get(serviceName).equals(group)){
                map.remove(serviceName);
                if(map.size()!=0){
                    String newValue = LionUtils.convertMapToRawLionConfigValue(map);
                    String url = "http://lionapi.dp:8080/config2/set?env="+configManager.getEnv()+"&id=2&key="+lionKey+"&value="+newValue;
                    System.out.println(url);
                    HttpCallUtils.httpGet(url);
                }else{
                    //TODO need delete api
                    String newValue = LionUtils.convertMapToRawLionConfigValue(map);
                    String url = "http://lionapi.dp:8080/config2/set?env="+configManager.getEnv()+"&id=2&key="+lionKey+"&value="+newValue;
                    System.out.println(url);
                    HttpCallUtils.httpGet(url);
                }
                return true;
            }else
                return true;
        }else
            return true;
    }

    @Override
    public boolean updateRouterConfig(String projectName, String serviceName, String type, String ipAddress, String group,
                                      String newIpAddress,String newGroup) {

        deleteRouterConfig(projectName,serviceName,type,ipAddress,group);
        if(addRouterConfig(projectName,serviceName,type,newIpAddress,newGroup))
            return true;
        else{
            addRouterConfig(projectName,serviceName,type,ipAddress,group);
            return false;
        }
    }

    @Override
    public boolean addRouterConfig(String projectName, String serviceName, String type, String ipAddress, String group) {
        String lionKey = projectName+".pigeon.group."+type+"."+ipAddress;
        String value = Lion.getStringValue(lionKey);
        System.out.println(value);
        if(value==null){
            String newValue = serviceName+":"+group;
            //TODO using LionUtils to create
            String url = "http://lionapi.dp:8080/config2/create?env="+configManager.getEnv()+"&id=2&project="+projectName+"&key="+lionKey+
                    "&desc=Auto%20generate%20router%20config";
            System.out.println(url);
            HttpCallUtils.httpGet(url);
            url = "http://lionapi.dp:8080/config2/set?env="+configManager.getEnv()+"&id=2&key="+lionKey+"&value="+newValue;
            System.out.println(url);
            HttpCallUtils.httpGet(url);
        }else{
            Map<String,String> map = LionUtils.convertRawLionConfigValue(value);
            if(map.containsKey(serviceName))
                return false;
            map.put(serviceName,group);
            String newValue = LionUtils.convertMapToRawLionConfigValue(map);
            String url = "http://lionapi.dp:8080/config2/set?env="+configManager.getEnv()+"&id=2&key="+lionKey+"&value="+newValue;
            System.out.println(url);
            HttpCallUtils.httpGet(url);
        }
        //TODO return the httpGet response value
        return true;
    }

    @Override
    public List<RouterInfo> reload(String projectName, String serviceName, String type) {
        Map<String,List<RouterInfo>> map = LionUtils.getServiceRouterConfigsPerProject(projectName,type,configManager.getEnv());
        map.get(serviceName);
        return map.get(serviceName);
    }

    private HashMap<String,RouterInfo> parseConfigsMap(List<RouterInfo> routerInfos) {
        Iterator<RouterInfo> iterator = routerInfos.iterator();
        HashMap<String, RouterInfo> map = new HashMap<String, RouterInfo>();
        while (iterator.hasNext()) {
            RouterInfo routerInfo = iterator.next();
            String key = routerInfo.getIpAddress() + ":" + routerInfo.getGroup();
            map.put(key, routerInfo);
        }
        return map;
    }
}
