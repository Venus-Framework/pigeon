/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.util.Constants;

public class ServerConfig {

	public static final int DEFAULT_PORT = 4625;
	private int port = DEFAULT_PORT;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private int corePoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_COREPOOLSIZE,
			Constants.DEFAULT_PROVIDER_COREPOOLSIZE);
	private int maxPoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_MAXPOOLSIZE,
			Constants.DEFAULT_PROVIDER_MAXPOOLSIZE);
	private int workQueueSize = configManager.getIntValue(Constants.KEY_PROVIDER_WORKQUEUESIZE,
			Constants.DEFAULT_PROVIDER_WORKQUEUESIZE);
	private String group;
	private String processType = configManager.getStringValue(Constants.KEY_PROCESS_TYPE,
			Constants.DEFAULT_PROCESS_TYPE);
	private Set<String> protocols = new HashSet<String>();

	public ServerConfig() {
		protocols.add("default");
		protocols.add("http");
	}
	
	public Set<String> getProtocols() {
		return protocols;
	}

	public void setProtocols(Set<String> protocols) {
		this.protocols = protocols;
	}

	public String getProcessType() {
		return processType;
	}

	public void setProcessType(String processType) {
		this.processType = processType;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
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

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
