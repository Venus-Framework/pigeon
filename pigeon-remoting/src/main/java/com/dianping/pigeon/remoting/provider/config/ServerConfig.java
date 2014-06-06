/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.config;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
	private boolean enableTest = configManager
			.getBooleanValue(Constants.KEY_TEST_ENABLE, Constants.DEFAULT_TEST_ENABLE);
	private int corePoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_COREPOOLSIZE,
			Constants.DEFAULT_PROVIDER_COREPOOLSIZE);
	private int maxPoolSize = configManager.getIntValue(Constants.KEY_PROVIDER_MAXPOOLSIZE,
			Constants.DEFAULT_PROVIDER_MAXPOOLSIZE);
	private int workQueueSize = configManager.getIntValue(Constants.KEY_PROVIDER_WORKQUEUESIZE,
			Constants.DEFAULT_PROVIDER_WORKQUEUESIZE);
	private String group = configManager.getGroup();
	private String protocol = Constants.PROTOCOL_DEFAULT;
	private String env;
	private String ip;

	public ServerConfig() {
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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
		this.httpPort = httpPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		if (!StringUtils.isBlank(group)) {
			this.group = group;
		}
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
