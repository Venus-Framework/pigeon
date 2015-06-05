/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.config;

import java.util.Properties;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.util.Constants;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class DefaultRegistryConfigManager implements RegistryConfigManager {

	private static Logger logger = LoggerLoader.getLogger(DefaultRegistryConfigManager.class);

	@Override
	public Properties getRegistryConfig() {
		ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
		
		String registryType = configManager.getStringValue(Constants.KEY_REGISTRY_TYPE, Constants.DEFAULT_REGISTRY_TYPE);
		String registryAddr = configManager.getStringValue(Constants.KEY_REGISTRY_ADDRESS, configManager.getConfigServerAddress());
		
		Properties properties = new Properties();
		properties.put(Constants.KEY_REGISTRY_TYPE, registryType);
		properties.put(Constants.KEY_REGISTRY_ADDRESS, registryAddr);

		return properties;
	}

}
