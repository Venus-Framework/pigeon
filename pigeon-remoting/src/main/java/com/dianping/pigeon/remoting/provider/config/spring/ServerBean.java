/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config.spring;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class ServerBean {

	private static final Logger logger = LoggerLoader.getLogger(ServerBean.class);

	private int port = ServerConfig.DEFAULT_PORT;
	private int httpPort = ServerConfig.DEFAULT_HTTP_PORT;
	private boolean autoSelectPort = true;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private int corePoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_COREPOOLSIZE,
			Constants.DEFAULT_PROVIDER_COREPOOLSIZE);
	private int maxPoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_MAXPOOLSIZE,
			Constants.DEFAULT_PROVIDER_MAXPOOLSIZE);
	private int workQueueSize = configManager.getIntValue(Constants.KEY_PROVIDER_WORKQUEUESIZE,
			Constants.DEFAULT_PROVIDER_WORKQUEUESIZE);
	private String group = configManager.getGroup();
	private volatile ServerConfig serverConfig;

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
		this.httpPort = httpPort;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public ServerConfig init() throws Exception {
		if (serverConfig == null) {
			synchronized (this) {
				if (serverConfig == null) {
					serverConfig = new ServerConfig();
					serverConfig.setPort(port);
					serverConfig.setGroup(group);
					serverConfig.setAutoSelectPort(autoSelectPort);
					serverConfig.setCorePoolSize(corePoolSize);
					serverConfig.setMaxPoolSize(maxPoolSize);
					serverConfig.setWorkQueueSize(workQueueSize);
					ServiceFactory.startupServer(serverConfig);
				}
			}
		}
		return serverConfig;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
