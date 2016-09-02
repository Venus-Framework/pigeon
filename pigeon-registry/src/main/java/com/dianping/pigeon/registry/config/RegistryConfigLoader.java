package com.dianping.pigeon.registry.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;

public class RegistryConfigLoader {

	private static final Logger logger = LoggerLoader.getLogger(RegistryConfigLoader.class);

	private static final String ENV_FILE = "/data/webapps/appenv";

	static volatile boolean isInitialized = false;

	/*
	 * Service config can be stored in /data/webapps/appenv or Registery Center.
	 * The priority of /data/webapps/appenv is higher than Register Center.
	 * 
	 * Service config will be published to ConfigManager. Other module can use
	 * ConfigManager to get service configs.
	 */
	public synchronized static void init() {
		if (!isInitialized) {
			// Properties config = loadDefaultConfig();
			Properties config = new Properties();

			try {
				Properties props = loadFromFile();
				config.putAll(props);
			} catch (IOException e) {
				logger.error("Failed to load config from " + ENV_FILE, e);
			}

			config = normalizeConfig(config);
			ConfigManagerLoader.getConfigManager().init(config);
			// RegistryManager.getInstance().init(config);
			isInitialized = true;
		}
	}

	private static Properties normalizeConfig(Properties props) {
		// Strip trailing whitespace in property values
		Properties newProps = new Properties();
		for (String key : props.stringPropertyNames()) {
			String value = props.getProperty(key);
			newProps.put(key, value.trim());
		}
		return newProps;
	}

	private static Properties loadFromFile() throws IOException {
		Properties props = new Properties();
		InputStream in = null;

		try {
			in = new FileInputStream(ENV_FILE);
			props.load(in);
		} catch (FileNotFoundException e) {
			logger.warn(ENV_FILE + " does not exist");
		} finally {
			if (in != null)
				in.close();
		}
		return props;
	}

}
