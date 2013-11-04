package com.dianping.pigeon.test.client_1.x.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigLoader {

	private static final String PROPERTIES_PATH = "/config/applicationContext.properties";

	private static final Logger logger = Logger.getLogger(ConfigLoader.class);
	
	public static Properties getLocalProperties() {
		Properties properties = new Properties();
		InputStream input = ConfigLoader.class.getResourceAsStream(PROPERTIES_PATH);
		if (input != null) {
			try {
				properties.load(input);
				input.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		return properties;
	}
	
}
