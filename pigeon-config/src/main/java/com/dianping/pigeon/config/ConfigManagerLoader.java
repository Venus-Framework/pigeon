package com.dianping.pigeon.config;

import com.dianping.pigeon.config.file.PropertiesFileConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.log.SimpleLogger;

public class ConfigManagerLoader {

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
	private static final Logger logger = LoggerLoader.getLogger(ConfigManagerLoader.class);
	private static final String KEY_LOG_DEBUG_ENABLE = "pigeon.log.debug.enable";

	static {
		if (configManager == null) {
			configManager = new PropertiesFileConfigManager();
		}
		logger.info("config manager:" + configManager);
		configManager.init();
		initLoggerConfig();
	}

	private static void initLoggerConfig() {
		try {
			SimpleLogger.setDebugEnabled(configManager.getBooleanValue(KEY_LOG_DEBUG_ENABLE, false));
		} catch (RuntimeException e) {
		}
		ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new InnerConfigChangeListener());
	}

	private static class InnerConfigChangeListener implements ConfigChangeListener {

		@Override
		public void onKeyUpdated(String key, String value) {
			if (key.endsWith(KEY_LOG_DEBUG_ENABLE)) {
				try {
					SimpleLogger.setDebugEnabled(Boolean.valueOf(value));
				} catch (RuntimeException e) {
				}
			}
		}

		@Override
		public void onKeyAdded(String key, String value) {

		}

		@Override
		public void onKeyRemoved(String key) {

		}

	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
