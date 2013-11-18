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

	private String url;

	private String version;

	private T service;

	private int port = ServerFactory.DEFAULT_PORT;

	public ProviderConfig(Class<T> serviceInterface, T service) {
		this.setServiceInterface(serviceInterface);
		this.setService(service);
	}

	public ProviderConfig(T service) {
		Class interfaceClass = service.getClass().getInterfaces()[0];
		this.setService(service);
		this.setServiceInterface(interfaceClass);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Class<T> getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class<T> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		if (url != null) {
			url = url.trim();
		}
		this.url = url;
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
