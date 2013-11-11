/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.config.RemotingConfigurer;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.serialize.SerializerFactory;

public class InvokerConfig<T> {
	public static final String CALL_SYNC = Constants.CALL_SYNC;
	public static final String CALL_CALLBACK = Constants.CALL_CALLBACK;
	public static final String CALL_ONEWAY = Constants.CALL_ONEWAY;
	public static final String CALL_FUTURE = Constants.CALL_FUTURE;

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private Class<T> serviceInterface;

	private String serviceName;

	private String serviceVersion;

	private String callMethod = Constants.CALL_SYNC;

	private byte serialize = SerializerFactory.SERIALIZE_HESSIAN;

	private int timeout = 2000;

	private ServiceCallback callback;

	private String group = configManager.getProperty(Constants.KEY_GROUP, Constants.DEFAULT_GROUP);

	private boolean writeBufferLimit = RemotingConfigurer.getDefaultWriteBufferLimit();

	private String loadbalance = "autoaware";

	private boolean timeoutRetry = false;

	private String cluster = "failFast";

	private int retries = 1;

	private String vip;

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public String getVip() {
		return vip;
	}

	public void setVip(String vip) {
		if (!StringUtils.isBlank(vip)) {
			this.vip = vip.trim();
		}
	}

	public Class<T> getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(Class<T> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public void setWriteBufferLimit(boolean writeBufferLimit) {
		this.writeBufferLimit = writeBufferLimit;
	}

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
		if (!StringUtils.isBlank(loadbalance)) {
			this.loadbalance = loadbalance.trim();
		}
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		if (!StringUtils.isBlank(cluster)) {
			this.cluster = cluster.trim();
		}
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public InvokerConfig(Class<T> serviceInterface, String serviceName, int timeout, String callMethod,
			String serialize, ServiceCallback callback, String group, boolean writeBufferLimit, String loadbalance,
			String cluster, int retries, boolean timeoutRetry, String vip) {
		this.setServiceInterface(serviceInterface);
		if (StringUtils.isBlank(serviceName) && serviceInterface != null) {
			this.setServiceName(serviceInterface.getCanonicalName());
		} else {
			this.setServiceName(serviceName);
		}
		this.setTimeout(timeout);
		this.setCallMethod(callMethod);
		this.setCallback(callback);
		this.setGroup(group);
		this.setWriteBufferLimit(writeBufferLimit);
		this.setCluster(cluster);
		this.setLoadbalance(loadbalance);
		this.setRetries(retries);
		this.setTimeoutRetry(timeoutRetry);
		this.setSerialize(serialize);
		this.setVip(vip);
	}

	public InvokerConfig(String serviceName, Class<T> serviceClass) {
		this.setServiceInterface(serviceClass);
		if (StringUtils.isBlank(serviceName) && serviceClass != null) {
			this.setServiceName(serviceClass.getCanonicalName());
		} else {
			this.setServiceName(serviceName);
		}
	}

	public InvokerConfig(Class<T> serviceClass) {
		this.setServiceInterface(serviceClass);
		this.setServiceName(serviceClass.getCanonicalName());
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
		if (!Constants.CALL_SYNC.equalsIgnoreCase(callMethod) && !Constants.CALL_CALLBACK.equalsIgnoreCase(callMethod)
				&& !Constants.CALL_FUTURE.equalsIgnoreCase(callMethod)
				&& !Constants.CALL_ONEWAY.equalsIgnoreCase(callMethod)) {

			throw new IllegalArgumentException("Pigeon call method only support[" + Constants.CALL_SYNC + ", "
					+ Constants.CALL_CALLBACK + ", " + Constants.CALL_FUTURE + ", " + Constants.CALL_ONEWAY + "].");
		}
		if (!StringUtils.isBlank(callMethod)) {
			this.callMethod = callMethod.trim();
		}
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
	public void setSerialize(String serialize) {
		if (serialize != null) {
			serialize = serialize.trim();
		}
		if (Constants.SERIALIZE_JAVA.equalsIgnoreCase(serialize)) {
			this.serialize = SerializerFactory.SERIALIZE_JAVA;
		} else if (Constants.SERIALIZE_HESSIAN.equalsIgnoreCase(serialize)) {
			this.serialize = SerializerFactory.SERIALIZE_HESSIAN;
		} else {
			throw new IllegalArgumentException("Only hessian and java serialize type supported now!");
		}
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
		if (callback != null) {
			setCallMethod(InvokerConfig.CALL_CALLBACK);
		}
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
		if (!StringUtils.isBlank(group)) {
			this.group = group.trim();
		}
	}

	public boolean isWriteBufferLimit() {
		return writeBufferLimit;
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
