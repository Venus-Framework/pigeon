package com.dianping.dpsf.api;

import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalance;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;

public class ProxyFactory<IFACE> {

	private static Logger logger = LoggerLoader.getLogger(ProxyFactory.class);

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private String serviceName;

	private Class<?> iface;

	private String serialize = Constants.SERIALIZE_HESSIAN;

	private String callMethod = Constants.CALL_SYNC;

	private String hosts;

	private String weight;

	private int timeout = 2000;

	private IFACE obj;

	private ServiceCallback callback;

	private String group = configManager.getGroup();

	private boolean useLion = false;

	/**
	 * 是否对写Buffer限制大小(对于channel使用到的queue buffer的大小限制, 避免OutOfMemoryError)
	 */
	private boolean writeBufferLimit = configManager.getBooleanValue(Constants.KEY_DEFAULT_WRITE_BUFF_LIMIT,
			Constants.DEFAULT_WRITE_BUFF_LIMIT);

	private String loadBalance = LoadBalanceManager.DEFAULT_LOADBALANCE;

	private Class<? extends LoadBalance> loadBalanceClass;

	private LoadBalance loadBalanceObj;

	public void init() throws Exception {
		InvokerConfig invokerConfig = new InvokerConfig(this.iface, this.serviceName, this.timeout, this.callMethod,
				this.serialize, this.callback, this.group, this.writeBufferLimit, this.loadBalance,
				Constants.CLUSTER_FAILFAST, 0, false, null, null, null);

		this.obj = (IFACE) ServiceFactory.getService(invokerConfig);
		configLoadBalance(invokerConfig);
	}

	private void configLoadBalance(InvokerConfig invokerConfig) {
		Object loadBalanceToSet = loadBalanceObj != null ? loadBalanceObj
				: (loadBalanceClass != null ? loadBalanceClass : (loadBalance != null ? loadBalance : null));
		if (loadBalanceToSet != null) {
			LoadBalanceManager.register(invokerConfig.getUrl(), group, loadBalanceToSet);
		}
	}

	public void setGroupRoute(String serviceName, String group, Set<String> connectSet) {
	}

	public IFACE getProxy() throws Exception {
		return this.obj;
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
	public void setIface(Class iface) {
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
	 * @return the weight
	 */
	public String getWeight() {
		return weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(String weight) {
		this.weight = weight;
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

	public void setUseLion(boolean useLion) {
		this.useLion = useLion;
	}

	public void setWriteBufferLimit(boolean writeBufferLimit) {
		this.writeBufferLimit = writeBufferLimit;
	}

}
