/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.config.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.beans.factory.FactoryBean;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.config.InvokerMethodConfig;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.util.ClassUtils;
import com.dianping.pigeon.util.CollectionUtils;

public class ReferenceBean implements FactoryBean {

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private String url;

	private String interfaceName;

	private String serialize = Constants.SERIALIZE_HESSIAN;

	private String callType = Constants.CALL_SYNC;

	private String cluster = Constants.CLUSTER_FAILFAST;

	/**
	 * 用于支持P2P调用的服务IP地址，也作为注册中心无法访问时的备用地址
	 */
	private String vip;

	/**
	 * zone配置，仅用于测试
	 */
	private String zone;

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

	/**
	 * 1. Random LoadBalance：随机，按权重设置随机概率，在一个截面上碰撞的概率高，但调用量越大分布越均匀，
	 * 而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。
	 * 
	 * 2. RoundRobin LoadBalance 轮循，按公约后的权重设置轮循比率，存在慢的提供者累积请求问题，
	 * 比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。
	 * 
	 * 3. AutoAware LoadBalance： 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
	 * 使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。
	 * 
	 * 4. ConsistentHash LoadBalance 一致性Hash，相同参数的请求总是发到同一提供者。
	 * 当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
	 * 
	 */
	private String loadBalance = LoadBalanceManager.DEFAULT_LOADBALANCE;

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

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
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

	public void init() throws Exception {
		if (StringUtils.isBlank(interfaceName)) {
			throw new IllegalArgumentException("invalid interface:" + interfaceName);
		}
		this.objType = ClassUtils.loadClass(this.classLoader, this.interfaceName.trim());
		InvokerConfig<?> invokerConfig = new InvokerConfig(this.objType, this.url, this.timeout, this.callType,
				this.serialize, this.callback, this.group, this.writeBufferLimit, this.loadBalance, this.cluster,
				this.retries, this.timeoutRetry, this.vip, this.version, this.protocol);
		invokerConfig.setClassLoader(classLoader);

		if (!CollectionUtils.isEmpty(methods)) {
			Map<String, InvokerMethodConfig> methodMap = new HashMap<String, InvokerMethodConfig>();
			invokerConfig.setMethods(methodMap);
			for (InvokerMethodConfig method : methods) {
				methodMap.put(method.getName(), method);
			}
		}
		this.obj = ServiceFactory.getService(invokerConfig);
		configLoadBalance();
	}

	private void configLoadBalance() {
		Object loadBalanceToSet = loadBalanceObj != null ? loadBalanceObj
				: (loadBalanceClass != null ? loadBalanceClass : (loadBalance != null ? loadBalance : null));
		if (loadBalanceToSet != null) {
			LoadBalanceManager.register(url, group, loadBalanceToSet);
		}
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
