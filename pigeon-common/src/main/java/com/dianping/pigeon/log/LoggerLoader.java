/**
 * 
 */
package com.dianping.pigeon.log;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.dianping.pigeon.config.LocalConfigLoader;

public class LoggerLoader {

	private static LoggerContext context = null;

	static {
		init();
	}

	public static synchronized void init() {
		String appName = LocalConfigLoader.getAppName();
		System.setProperty("app.name", appName);
		ConfigurationSource source;
		try {
			source = new ConfigurationSource(LoggerLoader.class.getResourceAsStream("/pigeon_log4j.xml"));
			context = Configurator.initialize(null, source);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static Logger getLogger(Class<?> className) {
		return getLogger(className.getName());
	}

	public static Logger getLogger(String name) {
		if (context == null) {
			init();
		}
		return context.getLogger(name);
	}
}
