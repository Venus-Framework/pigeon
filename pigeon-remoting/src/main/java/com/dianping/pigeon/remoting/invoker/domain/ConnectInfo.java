/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ConnectInfo {

	private String host;
	private int port;
	private ConcurrentHashMap<String, Integer> serviceNames;

	public ConnectInfo(String serviceName, String host, int port, int weight) {
		this(new ConcurrentHashMap<String, Integer>(), host, port);
		this.serviceNames.put(serviceName, weight);
	}

	private ConnectInfo(ConcurrentHashMap<String, Integer> serviceNames, String host, int port) {
		this.serviceNames = serviceNames;
		this.host = host;
		this.port = port;
	}

	public void addServiceNames(Map<String, Integer> serviceNames) {
		this.serviceNames.putAll(serviceNames);
	}

	public Map<String, Integer> getServiceNames() {
		return serviceNames;
	}

	/**
	 * @return the connect
	 */
	public String getConnect() {
		return host + ":" + port;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
