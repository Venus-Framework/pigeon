/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadAutoawareLoadBalance;

public class RemotingConfigurer {

	private static final Logger logger = LoggerLoader.getLogger(RemotingConfigurer.class);

	private static final ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static volatile RemotingConfigurer INSTANCE;

	private static final String KEY_LOADBALANCE = "pigeon.loadbalance";
	private static final String KEY_RECONNECT_INTERVAL = "pigeon.reconnect.interval";
	private static final String KEY_HEARTBEAT_INTERVAL = "pigeon.heartbeat.interval";
	private static final String KEY_HEARTBEAT_TIMEOUT = "pigeon.heartbeat.timeout";
	private static final String KEY_HEARTBEAT_DEADTHRESHOLD = "pigeon.heartbeat.dead.threshold";
	private static final String KEY_HEARTBEAT_HEALTHTHRESHOLD = "pigeon.heartbeat.health.threshold";
	private static final String KEY_HEARTBEAT_AUTOPICKOFF = "pigeon.heartbeat.autopickoff";
	private static final String KEY_SERVICE_NAMESPACE = "pigeon.service.namespace";
	private static final String KEY_WRITE_BUFFER_HIGH_WATER = "pigeon.channel.writebuff.high";
	private static final String KEY_WRITE_BUFFER_LOW_WATER = "pigeon.channel.writebuff.low";
	private static final String KEY_DEFAULT_WRITE_BUFF_LIMIT = "pigeon.channel.writebuff.defaultlimit";
	private static final String KEY_MONITOR_ENABLED = "pigeon.monitor.enabled";

	private static final long DEFAULT_RECONNECT_INTERVAL = 3000;
	private static final long DEFAULT_HEARTBEAT_INTERVAL = 3000;
	private static final long DEFAULT_HEARTBEAT_TIMEOUT = 5000;
	private static final long DEFAULT_HEARTBEAT_DEADCOUNT = 5;
	private static final long DEFAULT_HEARTBEAT_HEALTHCOUNT = 5;
	private static final boolean DEFAULT_HEARTBEAT_AUTOPICKOFF = true;
	private static final String DEFAULT_LOADBALANCE = LoadAutoawareLoadBalance.NAME;
	private static final String DEFAULT_SERVICE_NAMESPACE = "http://service.dianping.com/";
	private static final int DEFAULT_WRITE_BUFFER_HIGH_WATER = 35 * 1024 * 1024;
	private static final int DEFAULT_WRITE_BUFFER_LOW_WATER = 25 * 1024 * 1024;
	private static final boolean DEFAULT_WRITE_BUFF_LIMIT = false;
	private static final boolean DEFAULT_MONITOR_ENABLED = true;

	private static Map<String, Object> configCache = new HashMap<String, Object>();

	private RemotingConfigurer() {
	}

	public static RemotingConfigurer getInstance() {
		if (INSTANCE == null) {
			synchronized (RemotingConfigurer.class) {
				if (INSTANCE == null) {
					INSTANCE = new RemotingConfigurer();
				}
			}
		}
		return INSTANCE;
	}

	public static String getLoadBalance() {
		return getStringValue(KEY_LOADBALANCE, DEFAULT_LOADBALANCE);
	}

	public static boolean isHeartBeatAutoPickOff() {
		return getBooleanValue(KEY_HEARTBEAT_AUTOPICKOFF, DEFAULT_HEARTBEAT_AUTOPICKOFF);
	}

	public static long getReconnectInterval() {
		return getLongValue(KEY_RECONNECT_INTERVAL, DEFAULT_RECONNECT_INTERVAL);
	}

	public static long getHeartBeatInterval() {
		return getLongValue(KEY_HEARTBEAT_INTERVAL, DEFAULT_HEARTBEAT_INTERVAL);
	}

	/**
	 * 心跳是否超时的判断阀值
	 * 
	 * @return
	 */
	public static long getHeartBeatTimeout() {
		return getLongValue(KEY_HEARTBEAT_TIMEOUT, DEFAULT_HEARTBEAT_TIMEOUT);
	}

	/**
	 * 获取心跳检测服务端假死的依据, 心跳连续超时的次数值
	 * 
	 * @return
	 */
	public static long getHeartBeatDeadCount() {
		return getLongValue(KEY_HEARTBEAT_DEADTHRESHOLD, DEFAULT_HEARTBEAT_DEADCOUNT);
	}

	/**
	 * 获取心跳检测服务端正常的依据, 心跳连续正常返回的次数值
	 * 
	 * @return
	 */
	public static long getHeartBeatHealthCount() {
		return getLongValue(KEY_HEARTBEAT_HEALTHTHRESHOLD, DEFAULT_HEARTBEAT_HEALTHCOUNT);
	}

	/**
	 * 返回service命名的前缀空间,
	 * 如http://service.dianping.com/cacheService/cacheConfigService_1
	 * .0.0中的http://service.dianping.com/
	 * 
	 * @return
	 */
	public static String getServiceNameSpace() {
		return getStringValue(KEY_SERVICE_NAMESPACE, DEFAULT_SERVICE_NAMESPACE);
	}

	public static boolean getDefaultWriteBufferLimit() {
		return getBooleanValue(KEY_DEFAULT_WRITE_BUFF_LIMIT, DEFAULT_WRITE_BUFF_LIMIT);
	}

	public static int getWriteBufferHighWater() {
		return getIntValue(KEY_WRITE_BUFFER_HIGH_WATER, DEFAULT_WRITE_BUFFER_HIGH_WATER);
	}

	public static int getWriteBufferLowWater() {
		return getIntValue(KEY_WRITE_BUFFER_LOW_WATER, DEFAULT_WRITE_BUFFER_LOW_WATER);
	}
	
	public static boolean isMonitorEnabled() {
		return getBooleanValue(KEY_MONITOR_ENABLED, DEFAULT_MONITOR_ENABLED);
	}

	public static String getStringValue(String key, String defaultValue) {
		String value = (String) configCache.get(key);
		if (value == null) {
			value = configManager.getProperty(key, defaultValue);
			configCache.put(key, value);
		}
		return value;
	}

	public static long getLongValue(String key, long defaultValue) {
		Long value = (Long) configCache.get(key);
		if (value == null) {
			String strValue = configManager.getProperty(key);
			if (strValue != null) {
				value = Long.parseLong(strValue);
			} else {
				value = defaultValue;
			}
			configCache.put(key, value);
		}
		return value;
	}

	public static int getIntValue(String key, int defaultValue) {
		Integer value = (Integer) configCache.get(key);
		if (value == null) {
			String strValue = configManager.getProperty(key);
			if (strValue != null) {
				value = Integer.parseInt(strValue);
			} else {
				value = defaultValue;
			}
			configCache.put(key, value);
		}
		return value;
	}

	public static boolean getBooleanValue(String key, boolean defaultValue) {
		Boolean value = (Boolean) configCache.get(key);
		if (value == null) {
			String strValue = configManager.getProperty(key);
			if (strValue != null) {
				value = Boolean.parseBoolean(strValue);
			} else {
				value = defaultValue;
			}
			configCache.put(key, value);
		}
		return value;
	}

}
