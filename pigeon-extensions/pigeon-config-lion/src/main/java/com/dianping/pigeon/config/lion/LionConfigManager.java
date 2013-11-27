/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.lion;

import org.apache.log4j.Logger;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.config.AbstractConfigManager;
import com.dianping.pigeon.monitor.LoggerLoader;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class LionConfigManager extends AbstractConfigManager {

	private static Logger logger = LoggerLoader.getLogger(LionConfigManager.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.pigeon.config.ConfigManager#getProperty(java.lang.String)
	 */
	@Override
	public String doGetProperty(String key) throws Exception {
		return ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(key);
	}

	public String getConfigServerAddress() {
		return EnvZooKeeperConfig.getZKAddress();
	}

	public String doGetEnv() {
		return EnvZooKeeperConfig.getEnv();
	}

	@Override
	public String doGetAppName() {
		return "";
	}

	@Override
	public String doGetLocalIp() {
		return null;
	}

	@Override
	public String doGetGroup() {
		return "";
	}

	@Override
	public String doGetLocalProperty(String key) throws Exception {
		return null;
	}

}
