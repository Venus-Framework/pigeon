/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.config;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.registry.util.Constants;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class DefaultRegistryConfigManager implements RegistryConfigManager {

	private static Logger logger = Log4jLoader.getLogger(DefaultRegistryConfigManager.class);

	@Override
	public Properties getRegistryConfig() {
		ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
		
		String registryType = configManager.getProperty(Constants.KEY_REGISTRY_TYPE, Constants.DEFAULT_REGISTRY_TYPE);
		String registryAddr = configManager.getProperty(Constants.KEY_REGISTRY_ADDRESS, configManager.getAddress());
		
		Properties properties = new Properties();
		properties.put(Constants.KEY_REGISTRY_TYPE, registryType);
		properties.put(Constants.KEY_REGISTRY_ADDRESS, registryAddr);

		return properties;
	}

}
