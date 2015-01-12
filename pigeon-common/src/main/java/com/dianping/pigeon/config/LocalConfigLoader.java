package com.dianping.pigeon.config;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.dianping.pigeon.util.FileUtils;

public class LocalConfigLoader {

	private static final String DEV_PROPERTIES_PATH = "config/applicationContext.properties";

	private static final String PROPERTIES_PATH = "config/pigeon.properties";

	private static final String GLOBAL_PROPERTIES_PATH = "/data/webapps/config/pigeon/pigeon.properties";

	private static String appName = null;

	public static String getAppName() {
		if (appName == null) {
			try {
				URL appProperties = LocalConfigLoader.class.getResource("/META-INF/app.properties");
				if (appProperties == null) {
					appProperties = new URL("file:" + LocalConfigLoader.class.getResource("/").getPath()
							+ "/META-INF/app.properties");
					if (!new File(appProperties.getFile()).exists()) {
						appProperties = new URL("file:/data/webapps/config/app.properties");
					}
				}
				Properties properties = null;
				if (appProperties != null) {
					properties = FileUtils.readFile(appProperties.openStream());
					appName = properties.getProperty("app.name");
				}
			} catch (Throwable e) {
			}
			if (appName == null) {
				return "";
			}
		}
		return appName;
	}

	private static void loadProperties(Map<String, Object> results, Properties properties) {
		for (Iterator ir = properties.keySet().iterator(); ir.hasNext();) {
			String key = ir.next().toString();
			if (key.startsWith("#")) {
				continue;
			}
			String value = properties.getProperty(key);
			value = value.trim();
			results.put(key, value.trim());
		}
	}

	public static Map<String, Object> load(ConfigManager configManager) {
		Map<String, Object> results = new HashMap<String, Object>();
		try {
			loadProperties(results, FileUtils.readFile(new FileInputStream(GLOBAL_PROPERTIES_PATH)));
		} catch (Throwable e) {
		}
		try {
			loadProperties(
					results,
					FileUtils.readFile(Thread.currentThread().getContextClassLoader()
							.getResourceAsStream(PROPERTIES_PATH)));
		} catch (Throwable e) {
		}
		if (configManager != null) {
			String env = configManager.getEnv();
			if (ConfigConstants.ENV_DEV.equalsIgnoreCase(env) || ConfigConstants.ENV_ALPHA.equalsIgnoreCase(env)) {
				try {
					loadProperties(
							results,
							FileUtils.readFile(Thread.currentThread().getContextClassLoader()
									.getResourceAsStream(DEV_PROPERTIES_PATH)));
				} catch (Throwable e) {
				}
			}
			try {
				loadProperties(
						results,
						FileUtils.readFile(Thread.currentThread().getContextClassLoader()
								.getResourceAsStream("config/pigeon_" + env + ".properties")));
			} catch (Throwable e) {
			}
		}
		return results;
	}

}
