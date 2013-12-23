/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.domain;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: HostInfo.java, v 0.1 2013-7-31 上午10:43:05 jianhuihuang Exp $
 */
public class HostInfo {

	private String connect;
	private String host;
	private int port;
	private int weight;

	public HostInfo(String host, int port, int weight) {
		this.host = host;
		this.port = port;
		this.connect = host + ":" + port;
		this.weight = weight;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HostInfo) {
			HostInfo hp = (HostInfo) obj;
			return this.host.equals(hp.host) && this.port == hp.port;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return host.hashCode() + port;
	}

	@Override
	public String toString() {
		return "HostInfo [host=" + host + ", port=" + port + ", weight=" + weight + "]";
	}

	public String getConnect() {
		return connect;
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

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
