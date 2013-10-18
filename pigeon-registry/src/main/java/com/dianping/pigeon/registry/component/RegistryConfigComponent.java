package com.dianping.pigeon.registry.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.plugin.Component;
import com.dianping.pigeon.registry.cache.RegistryCache;

public class RegistryConfigComponent implements Component {

	private static final String PROPERTIES_PATH = "config/applicationContext.properties";

	private static final Logger logger = Logger.getLogger(RegistryConfigComponent.class);

	@Override
	public void init() {
		Properties properties = new Properties();
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_PATH);
		if (input != null) {
			try {
				properties.load(input);
				input.close();
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		init(properties);
	}

	public void init(Properties properties) {
		RegistryCache.getInstance().init(properties);
	}
}
