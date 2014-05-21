package com.dianping.pigeon.config;

import com.dianping.pigeon.extension.ExtensionLoader;

public class ConfigManagerLoader {

	public static final ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
