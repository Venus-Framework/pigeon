/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config;

import java.util.Map;

import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;

public class InvokerConfig<T> {
	public static final String CALL_SYNC = Constants.CALL_SYNC;
	public static final String CALL_CALLBACK = Constants.CALL_CALLBACK;
	public static final String CALL_ONEWAY = Constants.CALL_ONEWAY;
	public static final String CALL_FUTURE = Constants.CALL_FUTURE;

	public static final String PROTOCOL_HTTP = Constants.PROTOCOL_HTTP;
	public static final String PROTOCOL_DEFAULT = Constants.PROTOCOL_DEFAULT;

	public static final String SERIALIZE_HESSIAN = SerializerFactory.HESSIAN;
	public static final String SERIALIZE_JAVA = SerializerFactory.JAVA;
	public static final String SERIALIZE_PROTO = SerializerFactory.PROTO;
	public static final String SERIALIZE_JSON = SerializerFactory.JSON;
	public static final String SERIALIZE_FST = SerializerFactory.FST;

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private Class<T> serviceInterface;

	private String url;

	private String version;

	private String callType = Constants.CALL_SYNC;

	private byte serialize = SerializerFactory.SERIALIZE_HESSIAN;

	private int timeout = configManager.getIntValue(Constants.KEY_INVOKER_TIMEOUT, Constants.DEFAULT_INVOKER_TIMEOUT);

	private ServiceCallback callback;

	private String group = configManager.getGroup();

	private String loadbalance = LoadBalanceManager.DEFAULT_LOADBALANCE;

	private String regionPolicy = RegionPolicyManager.INSTANCE.DEFAULT_REGIONPOLICY;

	private boolean timeoutRetry = false;

	private String cluster = Constants.CLUSTER_FAILFAST;

	private int retries = 1;

	private String vip;

	private int maxRequests = configManager.getIntValue(Constants.KEY_INVOKER_MAXREQUESTS, 0);

	private String protocol = Constants.PROTOCOL_DEFAULT;

	private Map<String, InvokerMethodConfig> methods;

	private ClassLoader classLoader;

	private String secret;
	
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public Map<String, InvokerMethodConfig> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, InvokerMethodConfig> methods) {
		this.methods = methods;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getMaxRequests() {
		return maxRequests;
	}

	public void setMaxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		if (serviceInterface != null && !serviceInterface.isInterface()) {
			throw new IllegalArgumentException("'serviceInterface' must be an interface");
		}
		this.serviceInterface = serviceInterface;
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

	public String getRegionPolicy() {
		return regionPolicy;
	}

	public void setRegionPolicy(String regionPolicy) {
		if(StringUtils.isNotBlank(regionPolicy)) {
			this.regionPolicy = regionPolicy;
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

	public InvokerConfig(Class<T> serviceInterface, String url, int timeout, String callMethod, String serialize,
			ServiceCallback callback, String group, boolean writeBufferLimit, String loadbalance, String cluster,
			int retries, boolean timeoutRetry, String vip, String version, String protocol) {
		this.setServiceInterface(serviceInterface);
		this.setUrl(url);
		this.setTimeout(timeout);
		this.setCallType(callMethod);
		this.setCallback(callback);
		this.setGroup(group);
		this.setCluster(cluster);
		this.setLoadbalance(loadbalance);
		this.setRetries(retries);
		this.setTimeoutRetry(timeoutRetry);
		this.setSerialize(serialize);
		this.setVip(vip);
		this.setVersion(version);
		this.setProtocol(protocol);
	}

	public InvokerConfig(String url, Class<T> serviceInterface) {
		this.setServiceInterface(serviceInterface);
		this.setUrl(url);
	}

	public InvokerConfig(Class<T> serviceInterface) {
		this.setServiceInterface(serviceInterface);
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
	 * @return the callType
	 */
	public String getCallType() {
		return callType;
	}

	/**
	 * @param callType
	 *            the callType to set
	 */
	public void setCallType(String callType) {
		if (!Constants.CALL_SYNC.equalsIgnoreCase(callType) && !Constants.CALL_CALLBACK.equalsIgnoreCase(callType)
				&& !Constants.CALL_FUTURE.equalsIgnoreCase(callType)
				&& !Constants.CALL_ONEWAY.equalsIgnoreCase(callType)) {

			throw new IllegalArgumentException("Pigeon call mode only support[" + Constants.CALL_SYNC + ", "
					+ Constants.CALL_CALLBACK + ", " + Constants.CALL_FUTURE + ", " + Constants.CALL_ONEWAY + "].");
		}
		if (!StringUtils.isBlank(callType)) {
			this.callType = callType.trim();
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
		this.serialize = SerializerFactory.getSerialize(serialize);
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
			setCallType(InvokerConfig.CALL_CALLBACK);
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
