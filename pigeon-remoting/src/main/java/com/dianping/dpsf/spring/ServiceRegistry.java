/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.dpsf.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public final class ServiceRegistry {

	private static final Logger logger = LoggerLoader.getLogger(ServiceRegistry.class);

	private boolean publish = true;
	private Map<String, Object> services;
	private int port = ServerConfig.DEFAULT_PORT;
	private int httpPort = ServerConfig.DEFAULT_HTTP_PORT;
	private boolean autoSelectPort = true;
	private boolean cancelTimeout = Constants.DEFAULT_TIMEOUT_CANCEL;
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private int corePoolSize = Constants.PROVIDER_POOL_CORE_SIZE;
	private int maxPoolSize = Constants.PROVIDER_POOL_MAX_SIZE;
	private int workQueueSize = Constants.PROVIDER_POOL_QUEUE_SIZE;
	private boolean enableTest = configManager
			.getBooleanValue(Constants.KEY_TEST_ENABLE, Constants.DEFAULT_TEST_ENABLE);
	@Deprecated
	private int enginePort = ServerConfig.DEFAULT_HTTP_PORT;
	@Deprecated
	private String serviceType = "dp";
	@Deprecated
	private boolean enableEngine = true;

	public boolean isCancelTimeout() {
		return cancelTimeout;
	}

	public void setCancelTimeout(boolean cancelTimeout) {
		this.cancelTimeout = cancelTimeout;
	}

	@Deprecated
	public int getEnginePort() {
		return enginePort;
	}

	@Deprecated
	public void setEnginePort(int enginePort) {
		this.enginePort = enginePort;
	}

	@Deprecated
	public String getServiceType() {
		return serviceType;
	}

	@Deprecated
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	@Deprecated
	public boolean isEnableEngine() {
		return enableEngine;
	}

	@Deprecated
	public void setEnableEngine(boolean enableEngine) {
		this.enableEngine = enableEngine;
		this.setEnableTest(enableEngine);
	}

	public boolean isEnableTest() {
		return enableTest;
	}

	public void setEnableTest(boolean enableTest) {
		this.enableTest = enableTest;
	}

	public boolean isAutoSelectPort() {
		return autoSelectPort;
	}

	public void setAutoSelectPort(boolean autoSelectPort) {
		this.autoSelectPort = autoSelectPort;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		// this.httpPort = httpPort;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getWorkQueueSize() {
		return workQueueSize;
	}

	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void init() throws Exception {
		ServerConfig serverConfig = new ServerConfig();
		serverConfig.setPort(port);
		serverConfig.setAutoSelectPort(autoSelectPort);
		serverConfig.setCorePoolSize(corePoolSize);
		serverConfig.setMaxPoolSize(maxPoolSize);
		serverConfig.setWorkQueueSize(workQueueSize);
		serverConfig.setEnableTest(enableTest);
		List<ProviderConfig<?>> providerConfigList = new ArrayList<ProviderConfig<?>>();
		for (String url : services.keySet()) {
			ProviderConfig<Object> providerConfig = new ProviderConfig<Object>(services.get(url));
			providerConfig.setUrl(url);
			providerConfig.setServerConfig(serverConfig);
			providerConfig.setCancelTimeout(cancelTimeout);
			providerConfigList.add(providerConfig);
		}
		ServiceFactory.addServices(providerConfigList);
	}

	/**
	 * @return the publish
	 */
	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public Map<String, Object> getServices() {
		return services;
	}

	public void setServices(Map<String, Object> services) {
		this.services = services;
	}

}
