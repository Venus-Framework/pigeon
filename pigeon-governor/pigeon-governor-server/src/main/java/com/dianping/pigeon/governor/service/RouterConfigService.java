package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.config.ProjectConfigBean;
import com.dianping.pigeon.governor.bean.config.RouterInfo;

import java.util.List;

/**
 * Created by shihuashen on 16/5/24.
 */
public interface RouterConfigService {
     ProjectConfigBean getProjectConfig(String projectName);
     boolean deleteRouterConfig(String projectName,String serviceName,String type,String ipAddress,String group);
     boolean updateRouterConfig(String projectName,String serviceName,String type,String ipAddress,String group,String newIpAddress,String newGroup);
     boolean addRouterConfig(String projectName,String serviceName,String type,String ipAddress,String group);
     List<RouterInfo> reload(String projectName, String serviceName, String type);
}
