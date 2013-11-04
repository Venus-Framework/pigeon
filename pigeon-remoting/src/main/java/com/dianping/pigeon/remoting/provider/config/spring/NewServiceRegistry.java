/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ServiceConfigLoader;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.remoting.provider.loader.ProviderBootStrapLoader;

public class NewServiceRegistry {

	private static final Logger logger = Logger
			.getLogger(NewServiceRegistry.class);

	private String serviceName;
	private Object serviceImpl;
	private int port = ServerFactory.DEFAULT_PORT;
//	private String group;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Object getServiceImpl() {
		return serviceImpl;
	}

	public void setServiceImpl(Object serviceImpl) {
		this.serviceImpl = serviceImpl;
	}
	
//	public String getGroup() {
//		return group;
//	}
//	
//	public void setGroup(String group) {
//		this.group = group;
//	}

	/**
	 * 要确保只是启动一次！，调用Pigeon启动器，通过事件的机制来并行初始化，确保快速的启动。
	 * 
	 * @throws ServiceException
	 */
	public void init() throws ServiceException {
		ProviderBootStrapLoader.startup(port);
		
		if (serviceName == null) {
			serviceName = serviceImpl.getClass().getInterfaces()[0].getName();
		}
		ExtensionLoader.getExtension(ServiceFactory.class).addService(
				serviceName, serviceImpl, port);
	}
}
