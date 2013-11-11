/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.serialize.SerializerFactory;

public class InvokerMetaData {

	private String serviceName;

	private String callMethod;

	private byte serialize;

	private int timeout;

	private ServiceCallback callback;

	private String group;

	private boolean writeBufferLimit;

	private String loadbalance;

	private boolean timeoutRetry;

	public boolean isTimeoutRetry() {
		return timeoutRetry;
	}

	public void setTimeoutRetry(boolean timeoutRetry) {
		this.timeoutRetry = timeoutRetry;
	}

	public String getLoadbalance() {
		return loadbalance;
	}

	public void setLoadbalance(String loadbalance) {
		this.loadbalance = loadbalance;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	private String cluster;
	private int retries;

	public InvokerMetaData(String serviceName, int timeout, String callMethod, String serialize,
			ServiceCallback callback, String group, boolean writeBufferLimit, String loadbalance, String cluster,
			int retries, boolean timeoutRetry) {

		this.serviceName = serviceName;
		this.timeout = timeout;
		this.callMethod = callMethod;
		this.callback = callback;
		this.group = group;
		this.writeBufferLimit = writeBufferLimit;
		this.cluster = cluster;
		this.loadbalance = loadbalance;
		this.retries = retries;
		this.timeoutRetry = timeoutRetry;
		if (Constants.SERIALIZE_JAVA.equalsIgnoreCase(serialize)) {
			this.serialize = SerializerFactory.SERIALIZE_JAVA;
		} else if (Constants.SERIALIZE_HESSIAN.equalsIgnoreCase(serialize)) {
			this.serialize = SerializerFactory.SERIALIZE_HESSIAN;
		}

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
		this.serviceName = serviceName;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the callMethod
	 */
	public String getCallMethod() {
		return callMethod;
	}

	/**
	 * @param callMethod
	 *            the callMethod to set
	 */
	public void setCallMethod(String callMethod) {
		this.callMethod = callMethod;
	}

	/**
	 * @return the serialize
	 */
	public byte getSerialize() {
		return serialize;
	}

	/**
	 * @param serialize
	 *            the serialize to set
	 */
	public void setSerialize(byte serialize) {
		this.serialize = serialize;
	}

	/**
	 * @return the callback
	 */
	public ServiceCallback getCallback() {
		return callback;
	}

	/**
	 * @param callback
	 *            the callback to set
	 */
	public void setCallback(ServiceCallback callback) {
		this.callback = callback;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	public boolean isWriteBufferLimit() {
		return writeBufferLimit;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
