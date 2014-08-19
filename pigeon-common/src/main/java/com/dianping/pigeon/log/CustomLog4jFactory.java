/**
 * 
 */
package com.dianping.pigeon.log;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;

/**
 * <p>
 * Title: pigeonLog.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-9-2 下午05:58:39
 */
public class CustomLog4jFactory {

	private CustomLog4jFactory() {
	}

	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String LOGGER_NAME = "com.dianping.pigeon";
	public static final Logger rootLogger = new RootLogger(Level.WARN);
	private static LoggerRepository loggerRepository = null;
	private static Map<String, Logger> loggers = new HashMap<String, Logger>();

	private static Level parseLevel(String logLevel) {
		Level l = null;
		if (logLevel != null && logLevel.equalsIgnoreCase("debug")) {
			l = Level.DEBUG;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("warn")) {
			l = Level.WARN;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("error")) {
			l = Level.ERROR;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("fatal")) {
			l = Level.FATAL;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("off")) {
			l = Level.OFF;
		} else {
			l = Level.INFO;
		}
		return l;
	}

	private static synchronized void initLogger() {
		System.out.println("initializing pigeon logger");
		String logLevel = configManager.getStringValue("pigeon.log.level", "info");
		boolean logConsole = configManager.getBooleanValue("pigeon.log.console", true);
		String logSuffix = configManager.getAppName();
		Level level = parseLevel(logLevel);
		LoggerRepository lr = new Hierarchy(rootLogger);
		new DOMConfigurator()
				.doConfigure(CustomLog4jFactory.class.getClassLoader().getResource("pigeon_log4j.xml"), lr);
		rootLogger.setLevel(level);

		String osName = System.getProperty("os.name");
		String bizLogDir = null;
		if (osName != null && osName.toLowerCase().indexOf("windows") > -1) {
			bizLogDir = "c:/";
		}
		for (Enumeration<?> appenders = lr.getLogger(LOGGER_NAME).getAllAppenders(); appenders.hasMoreElements();) {
			Appender appender = (Appender) appenders.nextElement();
			if (FileAppender.class.isInstance(appender)) {
				FileAppender logFileAppender = (FileAppender) appender;
				// logFileAppender.setThreshold(level);
				String logFileName = logFileAppender.getFile();
				File deleteFile = new File(logFileName);
				if (StringUtils.isNotBlank(logSuffix)) {
					logFileName = logFileName.replace(".log", "." + logSuffix + ".log");
				}
				if (bizLogDir != null) {
					File logFile = new File(bizLogDir, logFileName);
					logFileName = logFile.getAbsolutePath();
				}
				if (StringUtils.isNotBlank(logSuffix) || bizLogDir != null) {
					logFileAppender.setFile(logFileName);
					logFileAppender.activateOptions();
					if (deleteFile.exists()) {
						deleteFile.delete();
					}
					System.out.println("pigeon log file:" + logFileName);
				}
			} else if (ConsoleAppender.class.isInstance(appender)) {
				ConsoleAppender consoleAppender = (ConsoleAppender) appender;
				if (!logConsole) {
					lr.getLogger(LOGGER_NAME).removeAppender(consoleAppender);
				}
			}
		}
		loggerRepository = lr;
	}

	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getLogger(String name) {
		if (loggerRepository == null) {
			initLogger();
		}
		Logger logger = loggerRepository.getLogger(name);
		loggers.put(name, logger);
		return logger;
	}

}
