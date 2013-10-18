/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ConnectInfo {

	public static final String PLACEHOLDER = ":";

	private String connect;

	private String host;

	private int port;

	private String serviceName;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	private int weight;

	// private Map<String, Integer> serviceNameAndWeights;

	public ConnectInfo(String serviceName, String connect, int weight) {

		this.serviceName = serviceName;
		this.weight = weight;
		this.connect = connect;
		String[] connectMetaData = connect.split(PLACEHOLDER);
		this.host = connectMetaData[0];
		this.port = Integer.parseInt(connectMetaData[1]);

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
