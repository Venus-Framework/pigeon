/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.util.IpUtils;

/**
 * @author xiangwu
 * @Sep 30, 2013
 * 
 */
public final class DefaultServiceFactory implements ServiceFactory {

	private static ConcurrentHashMap<String, Object> services = new ConcurrentHashMap<String, Object>();
	private static Registry registry = ExtensionLoader
			.getExtension(Registry.class);

	public DefaultServiceFactory() {
	}

	public void addServices(Map<String, Object> services, int port) throws ServiceException {
		for (String serviceName : services.keySet()) {
			addService(serviceName, services.get(serviceName), port);
		}
	}

	public void addService(String serviceName, Object service, int port)
			throws ServiceException {
		if (!services.containsKey(serviceName)) {
			try {
				String ip = IpUtils.getFirstLocalIp();
				String serviceAddress = ip + ":" + port;
				registry.publishServiceAddress(serviceName, serviceAddress);
			} catch (Exception e) {
				throw new ServiceException("", e);
			}
			services.putIfAbsent(serviceName, service);
		}
	}

	public Object getService(String serviceName) {
		return services.get(serviceName);
	}

	public Collection<String> getServiceNames() {
		return services.keySet();
	}

	@Override
	public boolean exits(String serviceName) {
		return services.containsKey(serviceName);
	}

	@Override
	public Map<String, Object> getAllServices() {
		return services;
	}

}
