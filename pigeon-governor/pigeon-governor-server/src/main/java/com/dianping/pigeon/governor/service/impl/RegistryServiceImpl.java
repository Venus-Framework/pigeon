package com.dianping.pigeon.governor.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.RegistryService;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.util.Constants;

@org.springframework.stereotype.Service("registrySerivce")
public class RegistryServiceImpl implements RegistryService {

	@Override
	public void registryUpdateService(Service oriService, Service newService) throws RegistryException {
		
		RegistryManager.getInstance().setServerService(
				newService.getName(),newService.getGroup(),newService.getHosts());
		
		Set<String> oriHostSet = new HashSet<String>();
        for(String host : oriService)
            oriHostSet.add(host);
        
        Set<String> hostSet = new HashSet<String>();
        for(String host : newService)
            hostSet.add(host);
        
        Collection<String> addSet = CollectionUtils.subtract(hostSet, oriHostSet);
        for(String host : addSet) {
        	RegistryManager.getInstance().setServerWeight(host, Constants.WEIGHT_DEFAULT);
        }
        
	}
	
	@Override
	public void registryCreateService(Service service) throws RegistryException {
		
		RegistryManager.getInstance().setServerService(
				service.getName(),service.getGroup(),service.getHosts());
		
		Set<String> hostSet = new HashSet<String>();
        for(String host : service)
            hostSet.add(host);
        
        for(String host : hostSet) {
        	RegistryManager.getInstance().setServerWeight(host, Constants.WEIGHT_DEFAULT);
        }
		
	}

	@Override
	public void registryDeleteService(Service service) throws RegistryException {
		
		RegistryManager.getInstance().delServerService(service.getName(), service.getGroup());
		
	}
}
