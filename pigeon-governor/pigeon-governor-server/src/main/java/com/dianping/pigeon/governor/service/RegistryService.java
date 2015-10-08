package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.registry.exception.RegistryException;

public interface RegistryService {

	public void registryUpdateService(Service oriService, Service newService) throws RegistryException;
	
	public void registryCreateService(Service service) throws RegistryException;
	
	public void registryDeleteService(Service service) throws RegistryException;
}
