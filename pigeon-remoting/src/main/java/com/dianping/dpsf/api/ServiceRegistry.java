/**
 * 
 */
package com.dianping.dpsf.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.provider.component.ProviderConfig;

/**
 * <p>
 * Title: ServiceBeanFactory.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-8-26 上午10:43:19
 */
public class ServiceRegistry {

	private static Logger logger = LoggerLoader.getLogger(ProxyFactory.class);

	private Map<String, Object> services;

	private int port = 20000;

	public static boolean isInit = false;

	private int corePoolSize = 100;
	private int maxPoolSize = 2000;
	private int workQueueSize = 100;

	public ServiceRegistry() {

	}

	public ServiceRegistry(int port) {
		this.port = port;
	}

	public void init() throws Exception {
		List<ProviderConfig<?>> providerConfigList = new ArrayList<ProviderConfig<?>>();
		for (String url : services.keySet()) {
			ProviderConfig providerConfig = new ProviderConfig(services.get(url));
			providerConfig.setUrl(url);
			providerConfig.setPort(port);
			providerConfigList.add(providerConfig);
		}
		try {
			ServiceFactory.publishServices(providerConfigList, port);
		} catch (RpcException e) {
			throw new ServiceException("", e);
		}
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param services
	 *            the services to set
	 */
	public void setServices(Map<String, Object> services) {
		this.services = services;
	}

	public void register(String serviceName, Object service) throws ServiceException {
		if (this.services == null) {
			this.services = new HashMap<String, Object>();
		}
		if (this.services.containsKey(serviceName)) {
			throw new ServiceException("service:" + serviceName + " has been existent");
		}
		this.services.put(serviceName, service);
	}

	/**
	 * @return the corePoolSize
	 */
	public int getCorePoolSize() {
		return corePoolSize;
	}

	/**
	 * @param corePoolSize
	 *            the corePoolSize to set
	 */
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	/**
	 * @return the maxPoolSize
	 */
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * @param maxPoolSize
	 *            the maxPoolSize to set
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * @return the workQueueSize
	 */
	public int getWorkQueueSize() {
		return workQueueSize;
	}

	/**
	 * @param workQueueSize
	 *            the workQueueSize to set
	 */
	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}

}
