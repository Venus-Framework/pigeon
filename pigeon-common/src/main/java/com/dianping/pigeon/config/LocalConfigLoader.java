package com.dianping.pigeon.config;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.dianping.pigeon.util.FileUtils;

public class LocalConfigLoader {

	private static final String DEV_PROPERTIES_PATH = "config/applicationContext.properties";

	private static final String PROPERTIES_PATH = "config/pigeon.properties";

	private static final String GLOBAL_PROPERTIES_PATH = "/data/webapps/config/pigeon/pigeon.properties";

	private static Map<String, Object> recentCache = new HashMap<String, Object>();

	private static ConfigManager configManager;

	private static boolean loadProperties(Map<String, Object> results, Properties properties) {
		boolean hasChange = false;
		for (Iterator ir = properties.keySet().iterator(); ir.hasNext();) {
			String key = ir.next().toString();
			if (key.startsWith("#")) {
				continue;
			}
			String value = properties.getProperty(key);
			value = value.trim();
			results.put(key, value.trim());
			if (recentCache.containsKey(key)) {
				String oldValue = (String) recentCache.get(key);
				oldValue = oldValue.trim();
				if (oldValue != null && value != null) {
					if (!oldValue.equals(value)) {
						if (configManager != null) {
							((AbstractConfigManager) configManager).onConfigUpdated(key, value);
						}
						hasChange = true;
					}
				}
			} else {
				if (configManager != null) {
					((AbstractConfigManager) configManager).onConfigAdded(key, value);
				}
				hasChange = true;
			}
		}
		return hasChange;
	}

	public static Map<String, Object> load(ConfigManager configManager) {
		Map<String, Object> results = new HashMap<String, Object>();
		LocalConfigLoader.configManager = configManager;
		boolean hasChange = false;
		try {
			boolean changed = loadProperties(results, FileUtils.readFile(new FileInputStream(GLOBAL_PROPERTIES_PATH)));
			hasChange = !hasChange ? changed : hasChange;
		} catch (Throwable e) {
		}
		try {
			boolean changed = loadProperties(
					results,
					FileUtils.readFile(Thread.currentThread().getContextClassLoader()
							.getResourceAsStream(PROPERTIES_PATH)));
			hasChange = !hasChange ? changed : hasChange;
		} catch (Throwable e) {
		}
		if (configManager != null) {
			String env = configManager.getEnv();
			if (ConfigConstants.ENV_DEV.equalsIgnoreCase(env) || ConfigConstants.ENV_ALPHA.equalsIgnoreCase(env)) {
				try {
					boolean changed = loadProperties(
							results,
							FileUtils.readFile(Thread.currentThread().getContextClassLoader()
									.getResourceAsStream(DEV_PROPERTIES_PATH)));
					hasChange = !hasChange ? changed : hasChange;
				} catch (Throwable e) {
				}
			}
			try {
				boolean changed = loadProperties(
						results,
						FileUtils.readFile(Thread.currentThread().getContextClassLoader()
								.getResourceAsStream("config/pigeon_" + env + ".properties")));
				hasChange = !hasChange ? changed : hasChange;
			} catch (Throwable e) {
			}
		}
		recentCache = results;
		if (hasChange) {
			if (configManager != null) {
				((AbstractConfigManager) configManager).onConfigChanged(results);
			}
		}
		return results;
	}

	public static void startConfigChangeMonitor() {
		Thread configChangeMonitor = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
						load(configManager);
					} catch (Throwable e) {
					}
				}
			}

		});
		configChangeMonitor.setName("Pigeon-Local-Config-Monitor");
		configChangeMonitor.setDaemon(false);
		configChangeMonitor.start();
	}
}
