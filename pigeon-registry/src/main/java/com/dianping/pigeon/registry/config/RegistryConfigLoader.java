package com.dianping.pigeon.registry.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.RegistryMeta;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;

public class RegistryConfigLoader {
	
	private static final Logger logger = LoggerLoader.getLogger(RegistryConfigLoader.class);
	
	private static final String ENV_FILE = "/data/webapps/appenv";
	
	/*
	 * Service config can be stored in /data/webapps/appenv or Registery Center.
	 * The priority of /data/webapps/appenv is higher than Register Center.
	 * 
	 * Service config will be published to ConfigManager. Other module can use
	 * ConfigManager to get service configs.
	 */
	public static void init() {
		Properties config = loadDefaultConfig();
		
		try {
			Properties props = loadFromRegistry();
			config.putAll(props);
		} catch (RegistryException e) {
			logger.error("Failed to load config from registry", e);
		}
		
		try {
			Properties props = loadFromFile();
			config.putAll(props);
		} catch (IOException e) {
			logger.error("Failed to load config from " + ENV_FILE, e);
		}
		
		config = normalizeConfig(config);
		ExtensionLoader.getExtension(ConfigManager.class).init(config);
		RegistryManager.getInstance().init(config);
	}
	
	private static Properties normalizeConfig(Properties props) {
		// Strip trailing whitespace in property values
		Properties newProps = new Properties();
		for(String key : props.stringPropertyNames()) {
			String value = props.getProperty(key);
			newProps.put(key, value.trim());
		}
		return newProps;
	}

	private static Properties loadDefaultConfig() {
		Properties props = new Properties();
		props.put(Constants.KEY_GROUP, Constants.DEFAULT_GROUP);
		props.put(Constants.KEY_WEIGHT, Constants.DEFAULT_WEIGHT);
		props.put(Constants.KEY_AUTO_REGISTER, Constants.DEFAULT_AUTO_REGISTER);
		return props;
	}

	private static Properties loadFromRegistry() throws RegistryException {
		RegistryMeta meta = RegistryManager.getInstance().getRegistryMeta();
		Properties props = new Properties();
		props.put(Constants.KEY_GROUP, meta.getGroup());
		props.put(Constants.KEY_WEIGHT, "" + meta.getWeight());
		props.put(Constants.KEY_AUTO_REGISTER, "" + meta.isAutoRegister());
		return props;
	}

	private static Properties loadFromFile() throws IOException {
		Properties props = new Properties();
		InputStream in = null;
		
		try {
			in = new FileInputStream(ENV_FILE);
			props.load(in);
		} catch (FileNotFoundException e) {
			logger.info(ENV_FILE + "does not exist");
		} finally {
			if(in != null)
				in.close();
		}
		return props;
	}

	private static boolean configLoaded(Properties props) {
		if(props == null || props.size()==0)
			return false;
		return props.containsKey(Constants.KEY_GROUP) ||
			   props.containsKey(Constants.KEY_WEIGHT) ||
			   props.containsKey(Constants.KEY_AUTO_REGISTER) ||
			   props.containsKey(Constants.KEY_LOCAL_IP);
	}
}
