/**
 * 
 */
package com.dianping.dpsf.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

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

	private Map<String, Object> services;
	private int port = 20000;
	private int httpPort = ServerConfig.DEFAULT_HTTP_PORT;
	private boolean autoSelectPort = true;
	public static boolean isInit = false;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private int corePoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_COREPOOLSIZE,
			Constants.DEFAULT_PROVIDER_COREPOOLSIZE);
	private int maxPoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_MAXPOOLSIZE,
			Constants.DEFAULT_PROVIDER_MAXPOOLSIZE);
	private int workQueueSize = configManager.getIntValue(Constants.KEY_PROVIDER_WORKQUEUESIZE,
			Constants.DEFAULT_PROVIDER_WORKQUEUESIZE);

	public ServiceRegistry() {

	}

	public ServiceRegistry(int port) {
		this.port = port;
	}

	public void init() throws Exception {
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setPort(port);
		serverConfig.setHttpPort(httpPort);
		serverConfig.setAutoSelectPort(autoSelectPort);
		serverConfig.setCorePoolSize(corePoolSize);
		serverConfig.setMaxPoolSize(maxPoolSize);
		serverConfig.setWorkQueueSize(workQueueSize);

		List<ProviderConfig<?>> providerConfigList = new ArrayList<ProviderConfig<?>>();
		for (String url : services.keySet()) {
			ProviderConfig providerConfig = new ProviderConfig(services.get(url));
			providerConfig.setUrl(url);
			providerConfig.setServerConfig(serverConfig);
			providerConfigList.add(providerConfig);
		}

		try {
			ServiceFactory.addServices(providerConfigList);
		} catch (RpcException e) {
			throw new ServiceException("", e);
		}
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public boolean isAutoSelectPort() {
		return autoSelectPort;
	}

	public void setAutoSelectPort(boolean autoSelectPort) {
		this.autoSelectPort = autoSelectPort;
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
