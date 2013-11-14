package com.dianping.pigeon.test.client.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;

public class ConfigLoader {

	private static final String PROPERTIES_PATH = "/config/applicationContext.properties";

	private static final Logger logger = LoggerLoader.getLogger(ConfigLoader.class);

	public static void init() {
		Properties properties = new Properties();
		InputStream input = ConfigLoader.class.getResourceAsStream(PROPERTIES_PATH);
		if (input != null) {
			try {
				properties.load(input);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e1) {
			}
		}
		try {
			ExtensionLoader.getExtension(ConfigManager.class).init(properties);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
