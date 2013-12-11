/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ConnectInfo {

	public static final String PLACEHOLDER = ":";
	private String connect;
	private String host;
	private int port;
	private Map<String, Integer> serviceNames;

	// private Map<String, Integer> serviceNameAndWeights;

	public ConnectInfo(String serviceName, String connect, int weight) {
		this(new HashMap<String, Integer>(), connect);
		this.serviceNames.put(serviceName, weight);
	}

	public ConnectInfo(Map<String, Integer> serviceNames, String connect) {
		this.serviceNames = serviceNames;
		this.connect = connect;
		String[] connectMetaData = connect.split(PLACEHOLDER);
		this.host = connectMetaData[0];
		this.port = Integer.parseInt(connectMetaData[1]);
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
		return connect;
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

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}