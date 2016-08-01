package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.registry.exception.RegistryException;

import java.util.Set;

public interface RegistryService {

	public void registryUpdateService(Service oriService, Service newService) throws RegistryException;
	
	public void registryCreateService(Service service) throws RegistryException;
	
	public void registryDeleteService(Service service) throws RegistryException;

	public String getServiceHosts(String serviceName, String group) throws RegistryException;

	public void registryUpdateService(String serviceName, String group,
									  Set<String> newHosts, Set<String> toAddHosts) throws RegistryException;
}