/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.dpsf.spring;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import com.dianping.pigeon.component.QueryString;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.config.RemotingConfigurer;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.component.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallback;
import com.dianping.pigeon.remoting.invoker.loader.InvocationHandlerLoader;
import com.dianping.pigeon.remoting.invoker.loader.InvokerBootStrapLoader;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationProxy;

/**
 * 
 * 请使用pigeon2.0的配置方式，后续新功能不再支持该配置方式，请尽快升级。 引用服务的入口，一般配置如下: <bean
 * id="echoService" class="com.dianping.dpsf.spring.ProxyBeanFactory"
 * init-method="init"> <property name="serviceName"
 * value="http://service.dianping.com/testService/echoService_1.0.0" />
 * <property name="iface" value="com.dianping.pigeon.test.EchoService" />
 * <property name="serialize" value="hessian" /> <property name="callMethod"
 * value="sync" /> <property name="timeout" value="5000" /> </bean>
 * 
 * @author jianhuihuang
 * @version $Id: ProxyBeanFactory.java, v 0.1 2013-6-18 上午11:13:51 jianhuihuang
 *          Exp $
 */
public class ProxyBeanFactory implements FactoryBean {

	private static final Logger logger = LoggerLoader.getLogger("pigeon_service");

	private static AtomicInteger groupId = new AtomicInteger(0);

	/**
	 * @deprecated 兼容pigein1.x，后续不在支持开发配置，默认使用appname+interfacename
	 */
	private String serviceName;

	private String iface;

	private String serialize = Constants.SERIALIZE_HESSIAN;

	private String callMethod = Constants.CALL_SYNC;

	/**
	 * 支持4种负责均衡方式： 1. Random LoadBalance：随机，按权重设置随机概率，在一个截面上碰撞的概率高，但调用量越大分布越均匀，
	 * 而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。已经实现..
	 * 
	 * 2. RoundRobin LoadBalance 轮循，按公约后的权重设置轮循比率，存在慢的提供者累积请求问题，
	 * 比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。--未实现
	 * 
	 * 3. LeastActive LoadBalance： 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
	 * 使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。---已经实现，默认的
	 * 
	 * 4. ConsistentHash LoadBalance 一致性Hash，相同参数的请求总是发到同一提供者。
	 * 当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。---未实现
	 * 
	 * 5. LeastSuccess LoadBalance，
	 * 当前调用成功率最高的优先分配（为了避免负载不均与，成功率前80%再按照LeastActive的方式选择）
	 */
	private String loadbalance = "autoaware";

	public String getLoadbalance() {
		return loadbalance;
	}

	public void setLoadbalance(String loadbalance) {
		this.loadbalance = loadbalance;
	}

	/**
	 * server 端和client端都有该逻辑。 1. Failover:失败自动切换，当出现失败，重试其它服务器。(缺省),
	 * 重试几次，使用retries参数 2. Failfast:快速失败，只发起一次调用，失败立即报错 3.
	 * Failsafe:失败安全，出现异常时，直接忽略 4. Failback:失败自动恢复，后台记录失败请求，定时重发,
	 * 重发次数，使用retries参数 5. Forking:并行调用多个服务器，只要一个成功即返回。 6.
	 * Broadcast:广播调用所有提供者，逐个调用，任意一台报错则报错。
	 */
	private String cluster = "failFast";

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	/**
	 * 用于支持P2P调用的服务IP地址，也作为注册中心无法访问时的备用地址
	 */
	private String vip;

	public String getVip() {
		return vip;
	}

	public void setVip(String vip) {
		this.vip = vip;
	}

	/**
	 * 用于支持P2P调用的服务IP地址，测试环境用（env=dev）
	 */
	private String testVip;

	public String getTestVip() {
		return testVip;
	}

	public void setTestVip(String testVip) {
		this.testVip = testVip;
	}

	/**
	 * zone配置，仅用于测试
	 */
	private String zone;

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getLoadBalance() {
		return loadBalance;
	}

	private int retries = 1;

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	private boolean timeoutRetry;

	public boolean isTimeoutRetry() {
		return timeoutRetry;
	}

	public void setTimeoutRetry(boolean timeoutRetry) {
		this.timeoutRetry = timeoutRetry;
	}

	/**
	 * @deprecated 后续不在支持调用配置
	 */

	@SuppressWarnings("unused")
	private String hosts;

	/**
	 * @deprecated 后续不在支持配置权重
	 */
	@SuppressWarnings("unused")
	private String weight;

	private int timeout = 2000;

	private Object obj;

	private Class<?> objType;

	private ServiceCallback callback;

	/**
	 * @deprecated
	 */
	private String group;

	/**
	 * @deprecated
	 */
	private String loadBalance;

	/**
	 * @deprecated
	 */
	private Class<? extends LoadBalance> loadBalanceClass;

	/**
	 * @deprecated
	 */
	private LoadBalance loadBalanceObj;

	@SuppressWarnings("unused")
	private boolean isTest = false;

	/**
	 * 是否对写Buffer限制大小(对于channel使用到的queue buffer的大小限制, 避免OutOfMemoryError)
	 */
	private boolean writeBufferLimit = RemotingConfigurer.getDefaultWriteBufferLimit();

	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public void init() throws ClassNotFoundException {
		InvokerBootStrapLoader.startup();
		InvokerConfig invokerMetaData = generatorServiceInvokeProxy();
		configLoadBalance();
		if (zone != null && !zone.isEmpty()) {
			String[] parts = serviceName.split(QueryString.PREFIX_REGEXP);
			QueryString qs = parts.length > 1 ? new QueryString(parts[1]) : new QueryString();
			qs.addParameter("zone", zone);
			serviceName = parts[0] + QueryString.PREFIX + qs;
		}
		ClientManager.getInstance().findAndRegisterClientFor(invokerMetaData.getServiceName(),
				invokerMetaData.getGroup(), invokerMetaData.getVip());
	}

	private void configLoadBalance() {
		Object loadBalanceToSet = loadBalanceObj != null ? loadBalanceObj
				: (loadBalanceClass != null ? loadBalanceClass : (loadBalance != null ? loadBalance : null));
		if (loadBalanceToSet != null) {
			LoadBalanceManager.register(serviceName, group, loadBalanceToSet);
		}
	}

	/**
	 * 生成本地服务ref的代理对象
	 * 
	 * @throws ClassNotFoundException
	 */
	private InvokerConfig generatorServiceInvokeProxy() throws ClassNotFoundException {
		this.objType = Class.forName(this.iface.trim());
		InvokerConfig invokerMetaData = new InvokerConfig(this.objType, this.serviceName, this.timeout,
				this.callMethod, this.serialize, this.callback, this.group, this.writeBufferLimit, this.loadbalance,
				this.cluster, this.retries, this.timeoutRetry, this.vip);

		this.obj = Proxy.newProxyInstance(
				ProxyBeanFactory.class.getClassLoader(),
				new Class[] { this.objType },
				new ServiceInvocationProxy(invokerMetaData, InvocationHandlerLoader
						.createInvokeHandler(invokerMetaData)));

		return invokerMetaData;
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
	 * @param serviceName
	 *            the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * @param callMethod
	 *            the callMethod to set
	 */
	public void setCallMethod(String callMethod) {
		this.callMethod = callMethod;
	}

	/**
	 * @param hosts
	 *            the hosts to set
	 */
	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @param iface
	 *            the iface to set
	 */
	public void setIface(String iface) {
		this.iface = iface;
	}

	/**
	 * @param serialize
	 *            the serialize to set
	 */
	public void setSerialize(String serialize) {
		this.serialize = serialize;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(String weight) {
		this.weight = weight;
	}

	/**
	 * @param callback
	 *            the callback to set
	 */
	public void setCallback(ServiceCallback callback) {
		this.callback = callback;
	}

	/**
	 * @deprecated
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

	/**
	 * @deprecated
	 * 
	 * @param isTest
	 */
	public void setIsTest(boolean isTest) {
		this.isTest = isTest;
	}

	public void setWriteBufferLimit(boolean writeBufferLimit) {
		this.writeBufferLimit = writeBufferLimit;
	}

}
