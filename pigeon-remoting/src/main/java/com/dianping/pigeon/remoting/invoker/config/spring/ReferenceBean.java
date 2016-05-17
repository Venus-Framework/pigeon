/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.pigeon.remoting.invoker.route.region.RegionPolicyManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.beans.factory.FactoryBean;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.config.InvokerMethodConfig;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.CollectionUtils;

public class ReferenceBean implements FactoryBean {

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private String url;

	private String interfaceName;

	private String serialize = Constants.SERIALIZE_HESSIAN;

	private String callType = Constants.CALL_SYNC;

	private String cluster = Constants.CLUSTER_FAILFAST;

	private String vip;

	private int retries = 1;

	private boolean timeoutRetry;

	private int timeout = configManager.getIntValue(Constants.KEY_INVOKER_TIMEOUT, Constants.DEFAULT_INVOKER_TIMEOUT);

	private Object obj;

	private Class<?> objType;

	private ServiceCallback callback;

	private String version;

	private String protocol;

	private List<InvokerMethodConfig> methods;

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

	public List<InvokerMethodConfig> getMethods() {
		return methods;
	}

	public void setMethods(List<InvokerMethodConfig> methods) {
		this.methods = methods;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private String group = configManager.getGroup();

	private String loadBalance = LoadBalanceManager.DEFAULT_LOADBALANCE;

	private String regionPolicy = RegionPolicyManager.INSTANCE.DEFAULT_REGIONPOLICY;

	private Class<? extends LoadBalance> loadBalanceClass;

	/**
	 * @deprecated
	 */
	private LoadBalance loadBalanceObj;

	/**
	 * 是否对写Buffer限制大小(对于channel使用到的queue buffer的大小限制, 避免OutOfMemoryError)
	 */
	private boolean writeBufferLimit = configManager.getBooleanValue(Constants.KEY_DEFAULT_WRITE_BUFF_LIMIT,
			Constants.DEFAULT_WRITE_BUFF_LIMIT);

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getVip() {
		return vip;
	}

	public void setVip(String vip) {
		this.vip = vip;
	}

	public String getLoadBalance() {
		return loadBalance;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public boolean isTimeoutRetry() {
		return timeoutRetry;
	}

	public void setTimeoutRetry(boolean timeoutRetry) {
		this.timeoutRetry = timeoutRetry;
	}

	public Object getObject() {
		return this.obj;
	}

	public Class<?> getObjectType() {
		return this.objType;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	/**
	 * @param serialize
	 *            the serialize to set
	 */
	public void setSerialize(String serialize) {
		this.serialize = serialize;
	}

	/**
	 * @param callback
	 *            the callback to set
	 */
	public void setCallback(ServiceCallback callback) {
		this.callback = callback;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	public void setLoadBalance(String loadBalance) {
		this.loadBalance = loadBalance;
	}

	public void setLoadBalanceClass(Class<? extends LoadBalance> loadBalanceClass) {
		this.loadBalanceClass = loadBalanceClass;
	}

	public void setLoadBalanceObj(LoadBalance loadBalanceObj) {
		this.loadBalanceObj = loadBalanceObj;
	}

	public void setWriteBufferLimit(boolean writeBufferLimit) {
		this.writeBufferLimit = writeBufferLimit;
	}

	public String getRegionPolicy() {
		return regionPolicy;
	}

	public void setRegionPolicy(String regionPolicy) {
		this.regionPolicy = regionPolicy;
	}

	public void init() throws Exception {
		if (StringUtils.isBlank(interfaceName)) {
			throw new IllegalArgumentException("invalid interface:" + interfaceName);
		}
		this.objType = ClassUtils.loadClass(this.classLoader, this.interfaceName.trim());
		InvokerConfig<?> invokerConfig = new InvokerConfig(this.objType, this.url, this.timeout, this.callType,
				this.serialize, this.callback, this.group, this.writeBufferLimit, this.loadBalance, this.cluster,
				this.retries, this.timeoutRetry, this.vip, this.version, this.protocol);
		invokerConfig.setClassLoader(classLoader);
		invokerConfig.setSecret(secret);
		invokerConfig.setRegionPolicy(regionPolicy);

		if (!CollectionUtils.isEmpty(methods)) {
			Map<String, InvokerMethodConfig> methodMap = new HashMap<String, InvokerMethodConfig>();
			invokerConfig.setMethods(methodMap);
			for (InvokerMethodConfig method : methods) {
				methodMap.put(method.getName(), method);
			}
		}
		this.obj = ServiceFactory.getService(invokerConfig);
		configLoadBalance(invokerConfig);
	}

	private void configLoadBalance(InvokerConfig invokerConfig) {
		Object loadBalanceToSet = loadBalanceObj != null ? loadBalanceObj
				: (loadBalanceClass != null ? loadBalanceClass : (loadBalance != null ? loadBalance : null));
		if (loadBalanceToSet != null) {
			LoadBalanceManager.register(invokerConfig.getUrl(), group, loadBalanceToSet);
		}
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
