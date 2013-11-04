/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.config;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.util.Constants;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class DefaultRegistryConfigManager implements RegistryConfigManager {

	private static Logger logger = Logger.getLogger(DefaultRegistryConfigManager.class);

	@Override
	public Properties getRegistryConfig() {
		ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
		
		String registryType = configManager.getProperty(Constants.KEY_REGISTRY_TYPE, Constants.DEFAULT_REGISTRY_TYPE);
		String registryAddr = configManager.getProperty(Constants.KEY_REGISTRY_ADDRESS, configManager.getAddress());
		String group = configManager.getProperty(Constants.KEY_GROUP, Constants.DEFAULT_GROUP);
		String weight = configManager.getProperty(Constants.KEY_WEIGHT, ""+Constants.DEFAULT_WEIGHT);
		String autoRegister = configManager.getProperty(Constants.KEY_AUTO_REGISTER, ""+Constants.DEFAULT_AUTO_REGISTER);
		
		Properties properties = new Properties();
		properties.put(Constants.KEY_REGISTRY_TYPE, registryType);
		properties.put(Constants.KEY_REGISTRY_ADDRESS, registryAddr);
		properties.put(Constants.KEY_GROUP, group);
		properties.put(Constants.KEY_WEIGHT, weight);
		properties.put(Constants.KEY_AUTO_REGISTER, autoRegister);

		return properties;
	}

}
