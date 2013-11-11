/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.lion;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;
import com.dianping.pigeon.config.AbstractConfigManager;
import com.dianping.pigeon.monitor.LoggerLoader;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class LionConfigManager extends AbstractConfigManager {

	private static Logger logger = LoggerLoader.getLogger(LionConfigManager.class);
	
	Properties localCache = new Properties();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.pigeon.config.ConfigManager#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		if(localCache.containsKey(key)) {
			if(logger.isInfoEnabled()) {
				logger.info("read from local cache with key[" + key + "]");
			}
			return "" + localCache.get(key);
		}
		try {
			String configVal = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(key);
			if (configVal != null) {
				return configVal.toString();
			} else {
				logger.error("config[key=" + key + "] not found in config server[lion].");
			}
		} catch (Exception e) {
			logger.error("error while reading property[" + key + "]:" + e.getMessage());
		}
		return null;
	}

	public String getEnv() {
		return EnvZooKeeperConfig.getEnv();
	}

	public String getAddress() {
		return EnvZooKeeperConfig.getZKAddress();
	}

	@Override
	public void init(Properties properties) {
		localCache.putAll(properties);
//		try {
//			ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).setPts(properties);
//		} catch (LionException e) {
//			throw new RuntimeException("init lion config manager failed", e);
//		}
	}
}
