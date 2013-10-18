/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.listener;

public class ServiceProviderChangeEvent {

	private String serviceName;
	private String host;
	private int port;
	private int weight;
	private String connect;

	public String getServiceName() {
		return serviceName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getWeight() {
		return weight;
	}

	public String getConnect() {
		return connect;
	}

	public ServiceProviderChangeEvent(String serviceName, String host, int port, int weight) {
		this.serviceName = serviceName;
		this.host = host;
		this.port = port;
		this.weight = weight;
		this.connect = host + ":" + port;
	}

}
