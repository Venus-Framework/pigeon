package com.dianping.pigeon.remoting.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;

public class ServiceConfigLoader {
	
	private static final Logger logger = Logger.getLogger(ServiceConfigLoader.class);
	
	private static final String ENV_FILE = "/data/webapps/appenv";
	
	/*
	 * Service config can be stored in /data/webapps/appenv or Registery Center.
	 * The priority of /data/webapps/appenv is higher than Register Center.
	 * 
	 * Service config will be published to ConfigManager. Other module can use
	 * ConfigManager to get service configs.
	 */
	public static void init() {
		Properties props = null;
		
		props = loadFromFile();
		
		if(!configLoaded(props)) {
			props = loadFromRegistry();
		}
		
		if(!configLoaded(props)) {
			props = loadDefaultConfig();
		} else {
			props = normalizeConfig(props);
		}
		
		ExtensionLoader.getExtension(ConfigManager.class).init(props);
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

	private static Properties loadFromRegistry() {
		// TODO Load service config from Registry Center
		return null;
	}

	private static Properties loadFromFile() {
		Properties props = new Properties();
		InputStream in = null;
		
		try {
			in = new FileInputStream(ENV_FILE);
			props.load(in);
		} catch (FileNotFoundException e) {
			logger.info(ENV_FILE + "does not exist");
		} catch (IOException e) {
			logger.error("Failed to load config from " + ENV_FILE, e);
		} finally {
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
		return props;
	}

	private static boolean configLoaded(Properties props) {
		if(props == null || props.size()==0)
			return false;
		return props.containsKey(Constants.KEY_GROUP);
	}
}
