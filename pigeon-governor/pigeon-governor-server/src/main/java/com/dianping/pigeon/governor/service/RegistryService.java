package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Service;

public interface RegistryService {

	public void registryUpdateService(Service oriService, Service newService) throws Exception;
	
	public void registryCreateService(Service service) throws Exception;
	
	public void registryDeleteService(Service service) throws Exception;
}
