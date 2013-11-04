package com.dianping.pigeon.test.server_1.x.loader;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;

public class ConfigLoader {

	private static final String PROPERTIES_PATH = "/config/applicationContext.properties";

	private static final Logger logger = Logger.getLogger(ConfigLoader.class);

	public static void initServer() throws Exception {
		Properties properties = new Properties();
		try {
			InputStream input = ConfigLoader.class.getResourceAsStream(PROPERTIES_PATH);
			if (input != null) {
				properties.load(input);
				input.close();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		String registryAddress = properties.getProperty("pigeon.registry.address");
		if (StringUtils.isBlank(registryAddress)) {
			registryAddress = EnvZooKeeperConfig.getZKAddress();
		}
		System.out.println("registry address:" + registryAddress);
		ConfigCache.getInstance(registryAddress);
	}

}
