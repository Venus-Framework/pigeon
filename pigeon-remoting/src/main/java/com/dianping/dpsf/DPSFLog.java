/**
 *
 */
package com.dianping.dpsf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;

import com.dianping.pigeon.log.LoggerLoader;

/**
 * <p>
 * Title: DPSFLog.java
 * </p>
 * <p>
 * Description: 描述
 * </p>
 * 
 * @author saber miao
 * @version 1.0
 * @created 2010-9-2 下午05:58:39
 */
public class DPSFLog {
	private DPSFLog() {
	}

	private static final String LOGGER_NAME = "dpsf";
	public static final Log log = LogFactory.getLog(LOGGER_NAME);
	public static final Logger rootLogger = new RootLogger(Level.DEBUG);

	static {
		initDPSFLog();
	}

	public static synchronized void initDPSFLog() {
		LoggerLoader.initLogger(LOGGER_NAME);
	}

	public static void centralLogWarn(String key1, String key2, String key3) {
		centralLogWarn(key1, key2, key3, 1);
	}

	public static void centralLogWarn(String key1, String key2, String key3, double value) {
		centralLogWarn(key1, key2, key3, value, 10000);
	}

	public static void centralLogWarn(String key1, String key2, String key3, double value, long cacheSpan) {
		centralLog("@pigeon-warn", key1, key2, key3, value, cacheSpan);
	}

	public static void centralLogError(String key1, String key2, String key3) {
		centralLogError(key1, key2, key3, 1);
	}

	public static void centralLogError(String key1, String key2, String key3, double value) {
		centralLogError(key1, key2, key3, value, 10000);
	}

	public static void centralLogError(String key1, String key2, String key3, double value, long cacheSpan) {
		centralLog("@pigeon-error", key1, key2, key3, value, cacheSpan);
	}

	public static void centralLog(String key1, String key2, String key3, String key4, double value, long cacheSpan) {
	}

	public static Logger getLogger() {
		return LoggerLoader.getLogger(LOGGER_NAME);
	}

}
