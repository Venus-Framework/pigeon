/**
 * 
 */
package com.dianping.pigeon.monitor;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;

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
public class Log4jLoader {
	private Log4jLoader() {
	}

	private static final String LOGGER_NAME = "pigeon";
	public static final Log log = LogFactory.getLog(LOGGER_NAME);
	public static final Logger rootLogger = new RootLogger(Level.DEBUG);
	private static LoggerRepository loggerRepository = null;
	private static Level level = Level.WARN;
	private static volatile boolean initOK = false;

	public static synchronized void initLogger(String className) {
		if (initOK) {
			return;
		}

		Properties logPro = new Properties();
		String logLevel = "warn";
		String logSuffix = "default";
		try {
			logPro.load(Log4jLoader.class.getClassLoader().getResourceAsStream("config/applicationContext.properties"));
			logLevel = logPro.get("pigeon.logLevel") == null ? null : logPro.get("pigeon.logLevel").toString();
			logSuffix = logPro.get("pigeon.logSuffix").toString();
		} catch (Exception e) {
			log.warn("no pigeon log config found in config/applicationContext.properties");
		}
		if (logSuffix == null || logSuffix.length() < 1) {
			try {
				logSuffix = logPro.get("app.prefix").toString();
			} catch (Exception e) {
				log.warn("no app.prefix found in config/applicationContext.properties");
			}
		}

		if (logLevel != null && logLevel.equalsIgnoreCase("debug")) {
			level = Level.DEBUG;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("info")) {
			level = Level.INFO;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("warn")) {
			level = Level.WARN;
		} else if (logLevel != null && logLevel.equalsIgnoreCase("error")) {
			level = Level.ERROR;
		}

		LoggerRepository lr = new Hierarchy(rootLogger);

		new DOMConfigurator().doConfigure(Log4jLoader.class.getClassLoader().getResource("pigeon_log4j.xml"), lr);

		String osName = System.getProperty("os.name");
		String bizLogDir = null;
		if (osName != null && osName.toLowerCase().indexOf("windows") > -1) {
			bizLogDir = "d:/";
		}
		FileAppender fileAppender = null;
		for (Enumeration<?> appenders = lr.getLogger(LOGGER_NAME).getAllAppenders(); (null == fileAppender)
				&& appenders.hasMoreElements();) {
			Appender appender = (Appender) appenders.nextElement();
			if (FileAppender.class.isInstance(appender)) {
				FileAppender logFileAppender = (FileAppender) appender;
				logFileAppender.setThreshold(level);
				String logFileName = logFileAppender.getFile();
				File deleteFile = new File(logFileName);
				if (logSuffix != null) {
					logFileName = logFileName.replace(".log", "." + logSuffix + ".log");
				}
				if (bizLogDir != null) {

					File logFile = new File(bizLogDir, logFileName);
					logFileName = logFile.getAbsolutePath();
				}
				if (logSuffix != null || bizLogDir != null) {
					logFileAppender.setFile(logFileName);
					logFileAppender.activateOptions();
					if (deleteFile.exists()) {
						deleteFile.delete();
					}
					log.warn(logFileAppender.getFile() + "的输出路径改变为:" + logFileName);
				}
			}
		}

		loggerRepository = lr;
		initOK = true;
	}

	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getLogger(String name) {
		if (loggerRepository == null) {
			initLogger(name);
		}
		Logger logger = loggerRepository.getLogger(name);
		logger.setLevel(level);
		return logger;
	}

}
