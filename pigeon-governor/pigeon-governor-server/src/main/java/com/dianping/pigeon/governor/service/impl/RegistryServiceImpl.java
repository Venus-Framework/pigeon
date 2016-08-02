package com.dianping.pigeon.governor.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.RegistryService;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.util.Constants;
import org.apache.commons.lang.StringUtils;

@org.springframework.stereotype.Service("registrySerivce")
public class RegistryServiceImpl implements RegistryService {

	private final RegistryManager registryManager = RegistryManager.getInstance();

	@Override
	public void registryUpdateService(Service oriService, Service newService) throws RegistryException {
		registryManager.setServerService(
				newService.getName(), newService.getGroup(), newService.getHosts());
		
		Set<String> oriHostSet = new HashSet<String>();
        for(String host : oriService)
            oriHostSet.add(host);
        
        Set<String> hostSet = new HashSet<String>();
        for(String host : newService)
            hostSet.add(host);
        
        Collection<String> addSet = CollectionUtils.subtract(hostSet, oriHostSet);
        for(String host : addSet) {
			registryManager.setServerWeight(host, Constants.WEIGHT_DEFAULT);
        }
        
	}
	
	@Override
	public void registryCreateService(Service service) throws RegistryException {
		registryManager.setServerService(
				service.getName(), service.getGroup(), service.getHosts());
		Set<String> hostSet = new HashSet<String>();

        for(String host : service)
            hostSet.add(host);
        
        for(String host : hostSet) {
			registryManager.setServerWeight(host, Constants.WEIGHT_DEFAULT);
        }
		
	}

	@Override
	public void registryDeleteService(Service service) throws RegistryException {
		registryManager.delServerService(service.getName(), service.getGroup());
	}

	@Override
	public String getServiceHosts(String serviceName, String group) throws RegistryException {

		return registryManager.getServiceHosts(serviceName, group);
	}

	/**
	 * 新节点weight设为pigeon默认weight
	 * @param serviceName
	 * @param group
	 * @param newHosts
	 * @param toAddHosts
	 * @throws RegistryException
	 */
	@Override
	public void registryUpdateService(String serviceName, String group,
									  Set<String> newHosts, Set<String> toAddHosts) throws RegistryException {
		registryManager.setServerService(serviceName, group, StringUtils.join(newHosts, ","));
		if (toAddHosts.size() > 0) {
			registryManager.setHostsWeight(serviceName, group,
					StringUtils.join(toAddHosts, ","), Constants.WEIGHT_DEFAULT);
		}
	}
}
