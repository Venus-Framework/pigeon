package com.dianping.pigeon.config;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.util.FileUtils;

public class LocalConfigLoader {

	private static Logger logger = Logger.getLogger(LocalConfigLoader.class);

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
						onConfigUpdated(key, value);
						hasChange = true;
					}
				}
			} else {
				onConfigAdded(key, value);
				hasChange = true;
			}
		}
		return hasChange;
	}

	private static void onConfigUpdated(String key, String value) {
		if (configManager != null) {
			List<ConfigChangeListener> listeners = configManager.getConfigChangeListeners();
			for (ConfigChangeListener listener : listeners) {
				listener.onKeyUpdated(key, value);
			}
		}
	}

	private static void onConfigAdded(String key, String value) {
		if (configManager != null) {
			List<ConfigChangeListener> listeners = configManager.getConfigChangeListeners();
			for (ConfigChangeListener listener : listeners) {
				listener.onKeyAdded(key, value);
			}
		}
	}

	private static void onConfigRemoved(String key, String value) {
		if (configManager != null) {
			List<ConfigChangeListener> listeners = configManager.getConfigChangeListeners();
			for (ConfigChangeListener listener : listeners) {
				listener.onKeyRemoved(key);
			}
		}
	}

	private static void onConfigChanged(Map<String, Object> properties) {
		if (configManager != null) {
			List<ConfigChangeListener> listeners = configManager.getConfigChangeListeners();
			for (ConfigChangeListener listener : listeners) {
				listener.onConfigChange(properties);
			}
		}
	}

	public static Map<String, Object> load(ConfigManager configManager) {
		Map<String, Object> results = new HashMap<String, Object>();
		LocalConfigLoader.configManager = configManager;
		boolean hasChange = false;
		try {
			boolean changed = loadProperties(results, FileUtils.readFile(new FileInputStream(GLOBAL_PROPERTIES_PATH)));
			hasChange = !hasChange ? changed : hasChange;
		} catch (Throwable e) {
			logger.error("", e);
		}
		try {
			boolean changed = loadProperties(
					results,
					FileUtils.readFile(Thread.currentThread().getContextClassLoader()
							.getResourceAsStream(PROPERTIES_PATH)));
			hasChange = !hasChange ? changed : hasChange;
		} catch (Throwable e) {
			logger.error("", e);
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
					logger.error("", e);
				}
			}
			try {
				boolean changed = loadProperties(
						results,
						FileUtils.readFile(Thread.currentThread().getContextClassLoader()
								.getResourceAsStream("config/pigeon_" + env + ".properties")));
				hasChange = !hasChange ? changed : hasChange;
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
		recentCache = results;
		if (hasChange) {
			onConfigChanged(results);
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
