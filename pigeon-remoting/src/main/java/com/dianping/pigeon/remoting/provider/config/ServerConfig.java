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

	public static final int DEFAULT_PORT = 4040;
	public static final int DEFAULT_HTTP_PORT = 4080;
	private int port = DEFAULT_PORT;
	private int httpPort = DEFAULT_HTTP_PORT;
	private boolean autoSelectPort = true;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private int corePoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_COREPOOLSIZE,
			Constants.DEFAULT_PROVIDER_COREPOOLSIZE);
	private int maxPoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_MAXPOOLSIZE,
			Constants.DEFAULT_PROVIDER_MAXPOOLSIZE);
	private int workQueueSize = configManager.getIntValue(Constants.KEY_PROVIDER_WORKQUEUESIZE,
			Constants.DEFAULT_PROVIDER_WORKQUEUESIZE);
	private String group;
	private Set<String> protocols = new HashSet<String>();

	public ServerConfig() {
		protocols.add(Constants.PROTOCOL_DEFAULT);
		protocols.add(Constants.PROTOCOL_HTTP);
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
		this.httpPort = httpPort;
	}

	public Set<String> getProtocols() {
		return protocols;
	}

	public void setProtocols(Set<String> protocols) {
		this.protocols = protocols;
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
