/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.config;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class DefaultRegistryConfigManager implements RegistryConfigManager {

	private final String KEY_ADDR = "pigeon.registry.address";
	private final String KEY_TYPE = "pigeon.registry.type";
	private static Logger logger = Logger.getLogger(DefaultRegistryConfigManager.class);

	@Override
	public Properties getRegistryConfig() {
		ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
		String registryServer = null;
		String registryType = null;
		try {
			registryServer = configManager.getProperty(KEY_ADDR);
		} catch (Exception e) {
			logger.error("Error while reading config:" + KEY_ADDR, e);
		}
		try {
			registryType = configManager.getProperty(KEY_TYPE);
		} catch (Exception e) {
			logger.error("Error while reading config:" + KEY_TYPE, e);
		}
		if (StringUtils.isBlank(registryServer)) {
			// throw new
			// IllegalArgumentException("No config found in config server with key:"
			// + KEY_ZKADDR);
			logger.warn("No config found in config server with key:" + KEY_ADDR);
			registryServer = configManager.getAddress();
			//registryServer = "127.0.0.1:2181";
			logger.warn("use default registry server address:" + registryServer);
		}
		if (StringUtils.isBlank(registryType)) {
			logger.warn("No config found in config server with key:" + KEY_TYPE);
			registryType = "zookeeper";
			logger.warn("use default registry type:" + registryType);
		}

		Properties properties = new Properties();
		properties.put("registryType", registryType);
		properties.put("registryServer", registryServer);

		return properties;
	}

}
