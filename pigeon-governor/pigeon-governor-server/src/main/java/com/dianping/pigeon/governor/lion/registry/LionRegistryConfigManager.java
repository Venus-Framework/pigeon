package com.dianping.pigeon.governor.lion.registry;

import java.util.Properties;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.config.RegistryConfigManager;
import com.dianping.pigeon.registry.util.Constants;

public class LionRegistryConfigManager implements RegistryConfigManager {

	private static Logger logger = LoggerLoader.getLogger(LionRegistryConfigManager.class);

	@Override
	public Properties getRegistryConfig() {
		ConfigManager configManager = ConfigManagerLoader.getConfigManager();
		
		String registryType = configManager.getStringValue(Constants.KEY_REGISTRY_TYPE, Constants.DEFAULT_REGISTRY_TYPE);
		String registryAddr = configManager.getStringValue("pigeon-governor-server.lion.zkserver", configManager.getConfigServerAddress());
		
		Properties properties = new Properties();
		properties.put(Constants.KEY_REGISTRY_TYPE, registryType);
		properties.put(Constants.KEY_REGISTRY_ADDRESS, registryAddr);

		return properties;
	}
}
