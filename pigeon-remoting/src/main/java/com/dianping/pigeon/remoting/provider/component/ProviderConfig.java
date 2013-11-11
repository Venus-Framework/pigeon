/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.component;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.provider.ServerFactory;

public class ProviderConfig<T> {

	private Class<T> serviceInterface;

	private String serviceName;

	private String serviceVersion;

	private T service;

	private int port = ServerFactory.DEFAULT_PORT;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public Class<T> getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class<T> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		if (serviceName != null) {
			serviceName = serviceName.trim();
		}
		this.serviceName = serviceName;
	}

	public T getService() {
		return service;
	}

	public void setService(T service) {
		this.service = service;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
