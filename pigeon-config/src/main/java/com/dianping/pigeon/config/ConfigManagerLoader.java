package com.dianping.pigeon.config;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.file.PropertiesFileConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;

public class ConfigManagerLoader {

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private static final Logger logger = LoggerLoader.getLogger(ConfigManagerLoader.class);

	static {
		if (configManager == null) {
			configManager = new PropertiesFileConfigManager();
		}
		logger.info("config manager:" + configManager);
		configManager.init();
	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
