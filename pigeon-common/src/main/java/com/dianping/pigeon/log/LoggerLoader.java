/**
 * 
 */
package com.dianping.pigeon.log;

import java.util.zip.Deflater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.dianping.pigeon.util.AppUtils;

public class LoggerLoader {

	private static LoggerContext context = null;

	public static String LOG_ROOT = System.getProperty("pigeon.log.dir", "/data/applogs/pigeon");

	public static final String KEY_LOG_DEBUG_ENABLE = "pigeon.log.debug.enable";

	static {
		init();
	}

	public static synchronized void init() {
		String appName = AppUtils.getAppName();
		System.setProperty("app.name", appName);

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		Layout layout = PatternLayout.createLayout(
				"%d [%t] %-5p [%c{2}] %m%n", config, null, null, true, false,
				null, null);

		// file info
		Filter fileInfoFilter = ThresholdFilter.createFilter(Level.ERROR, Result.DENY, Result.ACCEPT);
		Appender fileInfoAppender = RollingFileAppender.createAppender(LOG_ROOT + "/pigeon." + appName + ".log",
				LOG_ROOT + "/pigeon." + appName + ".log.%d{yyyy-MM-dd}.gz", "true", "FileInfo", "true", "4000",
				"true", TimeBasedTriggeringPolicy.createPolicy("1", "true"),
				DefaultRolloverStrategy.createStrategy("30", "1", null, Deflater.DEFAULT_COMPRESSION + "", config),
				layout, fileInfoFilter, "false", null, null, config);
		fileInfoAppender.start();
		config.addAppender(fileInfoAppender);
		AppenderRef fileInfoRef = AppenderRef.createAppenderRef("FileInfo", null, fileInfoFilter);

		// console error
		Appender consoleErrorAppender = ConsoleAppender.createAppender(layout, null, "SYSTEM_ERR", "ConsoleError",
				"false", "false");
		config.addAppender(consoleErrorAppender);
		consoleErrorAppender.start();
//		AppenderRef consoleErrorAppenderRef = AppenderRef.createAppenderRef("ConsoleError", Level.ERROR, null);
//		AsyncAppender asyncConsoleErrorAppender = AsyncAppender.createAppender(
//				new AppenderRef[] { consoleErrorAppenderRef }, null, true, 128, "AsyncConsoleError", false, null,
//				config, false);
//		consoleErrorAppenderRef.start();
//		config.addAppender(consoleErrorAppenderRef);
//		config.addAppender(consoleErrorAppender);
//		AppenderRef asyncConsoleErrorRef = AppenderRef.createAppenderRef("AsyncConsoleError", Level.ERROR, null);

		// console warn
		Filter consoleWarnFilter = ThresholdFilter.createFilter(Level.ERROR, Result.DENY, Result.NEUTRAL);
		Appender consoleWarnAppender = ConsoleAppender.createAppender(layout, consoleWarnFilter, "SYSTEM_OUT",
				"ConsoleWarn", "false", "false");
		config.addAppender(consoleWarnAppender);
		consoleWarnAppender.start();
		AppenderRef consoleWarnAppenderRef = AppenderRef
				.createAppenderRef("ConsoleWarn", Level.WARN, consoleWarnFilter);
		AppenderRef consoleErrorAppenderRef = AppenderRef
				.createAppenderRef("ConsoleError", Level.WARN, null);

		AppenderRef[] refs = new AppenderRef[] { consoleErrorAppenderRef, consoleWarnAppenderRef, fileInfoRef };
		LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.INFO, "com.dianping.pigeon", "true", refs,
				null, config, null);
		loggerConfig.addAppender(consoleErrorAppender, Level.ERROR, null);
		loggerConfig.addAppender(consoleWarnAppender, Level.WARN, null);
		loggerConfig.addAppender(fileInfoAppender, Level.INFO, null);

		config.addLogger("com.dianping.pigeon", loggerConfig);

		// access info
		Appender accessInfoAppender = RollingFileAppender.createAppender(LOG_ROOT + "/pigeon." + appName
				+ ".access.log", LOG_ROOT + "/pigeon." + appName + ".log.access.%d{yyyy-MM-dd}.gz", "true",
				"AccessInfo", "true", "4000", "false", TimeBasedTriggeringPolicy.createPolicy("1", "true"),
				DefaultRolloverStrategy.createStrategy("30", "1", null, Deflater.DEFAULT_COMPRESSION + "", config),
				layout, fileInfoFilter, "false", null, null, config);
		accessInfoAppender.start();
		config.addAppender(accessInfoAppender);
		AppenderRef accessInfoRef = AppenderRef.createAppenderRef("AccessInfo", Level.INFO, null);

		AsyncAppender asyncAccessInfoAppender = AsyncAppender.createAppender(new AppenderRef[] { accessInfoRef }, null,
				true, 128, "AsyncAccessInfo", false, null, config, false);
		config.addAppender(asyncAccessInfoAppender);
		asyncAccessInfoAppender.start();
		AppenderRef asyncAccessInfoRef = AppenderRef.createAppenderRef("AsyncAccessInfo", Level.INFO, null);

		LoggerConfig accessLoggerConfig = LoggerConfig.createLogger("false", Level.INFO, "pigeon-access", "true",
				new AppenderRef[] { asyncAccessInfoRef }, null, config, null);
		config.addLogger("pigeon-access", accessLoggerConfig);
		accessLoggerConfig.addAppender(asyncAccessInfoAppender, Level.INFO, null);

		ctx.updateLoggers();

		context = ctx;
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

	public static LoggerContext getLoggerContext() {
		return context;
	}
}
