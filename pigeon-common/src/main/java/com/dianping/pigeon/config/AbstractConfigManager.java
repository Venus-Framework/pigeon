/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.util.FileUtils;
import com.dianping.pigeon.util.NetUtils;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public abstract class AbstractConfigManager implements ConfigManager {

	private static Logger logger = Logger.getLogger(AbstractConfigManager.class);

	public static final String KEY_GROUP = "swimlane";

	public static final String KEY_LOCAL_IP = "host.ip";

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_ENV = "environment";

	public static final String DEFAULT_GROUP = "";

	public static final int DEFAULT_WEIGHT = 1;

	private static final String DEV_PROPERTIES_PATH = "config/applicationContext.properties";

	private static final String PROPERTIES_PATH = "config/pigeon.properties";

	private static List<ConfigChangeListener> configChangeListeners = new ArrayList<ConfigChangeListener>();

	protected Map<String, Object> localCache = new HashMap<String, Object>();

	public abstract String doGetProperty(String key) throws Exception;

	public abstract String doGetLocalProperty(String key) throws Exception;

	public abstract String doGetEnv() throws Exception;

	public abstract String doGetLocalIp() throws Exception;

	public abstract String doGetAppName() throws Exception;

	public abstract String doGetGroup() throws Exception;

	public abstract void doSetStringValue(String key, String value) throws Exception;

	public abstract void doDeleteKey(String key) throws Exception;

	public AbstractConfigManager() {
		try {
			init(FileUtils
					.readFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_PATH)));
		} catch (Exception e) {
			logger.error("", e);
		}
		if (ConfigConstants.ENV_DEV.equalsIgnoreCase(getEnv())) {
			try {
				init(FileUtils.readFile(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(DEV_PROPERTIES_PATH)));
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	public boolean getBooleanValue(String key, boolean defaultValue) {
		Boolean value = getBooleanValue(key);
		return value != null ? value : defaultValue;
	}

	public Boolean getBooleanValue(String key) {
		return getProperty(key, Boolean.class);
	}

	public long getLongValue(String key, long defaultValue) {
		Long value = getLongValue(key);
		return value != null ? value : defaultValue;
	}

	public Long getLongValue(String key) {
		return getProperty(key, Long.class);
	}

	public int getIntValue(String key, int defaultValue) {
		Integer value = getIntValue(key);
		return value != null ? value : defaultValue;
	}

	public Integer getIntValue(String key) {
		return getProperty(key, Integer.class);
	}

	public float getFloatValue(String key, float defaultValue) {
		Float value = getFloatValue(key);
		return value != null ? value : defaultValue;
	}

	public Float getFloatValue(String key) {
		return getProperty(key, Float.class);
	}

	@Override
	public String getStringValue(String key, String defaultValue) {
		String value = getStringValue(key);
		return value != null ? value : defaultValue;
	}

	public String getLocalStringValue(String key) {
		return getPropertyFromLocal(key, String.class);
	}

	private <T> T getPropertyFromLocal(String key, Class<T> type) {
		String strValue = null;
		if (localCache.containsKey(key)) {
			Object value = localCache.get(key);
			// if (value != null && logger.isInfoEnabled()) {
			// logger.info("read from local cache with key[" + key + "]:" +
			// value);
			// }
			if (value.getClass() == type) {
				return (T) value;
			} else {
				strValue = value + "";
			}
		}
		if (strValue == null) {
			strValue = System.getProperty(key);
		}
		if (strValue == null) {
			try {
				strValue = doGetLocalProperty(key);
				if (strValue != null && logger.isInfoEnabled()) {
					logger.info("read from local config with key[" + key + "]:" + strValue);
				}
			} catch (Exception e) {
				logger.error("error while reading local config[" + key + "]:" + e.getMessage());
			}
		}
		if (strValue != null) {
			Object value = null;
			if (String.class == type) {
				value = strValue;
			} else if (!StringUtils.isBlank(strValue)) {
				if (Integer.class == type) {
					value = Integer.valueOf(strValue);
				} else if (Long.class == type) {
					value = Long.valueOf(strValue);
				} else if (Float.class == type) {
					value = Float.valueOf(strValue);
				} else if (Boolean.class == type) {
					value = Boolean.valueOf(strValue);
				}
			}
			if (value != null) {
				localCache.put(key, value);
			}
			return (T) value;
		} else {
			logger.info("config[key=" + key + "] not found, use default value");
		}
		return null;
	}

	@Override
	public String getStringValue(String key) {
		return getProperty(key, String.class);
	}

	private <T> T getProperty(String key, Class<T> type) {
		String strValue = null;
		if (localCache.containsKey(key)) {
			Object value = localCache.get(key);
			if (value != null && logger.isInfoEnabled()) {
				logger.info("read from local cache with key[" + key + "]:" + value);
			}
			if (value.getClass() == type) {
				return (T) value;
			} else {
				strValue = value + "";
			}
		}
		if (strValue == null) {
			strValue = System.getProperty(key);
		}
		if (strValue == null) {
			try {
				strValue = doGetLocalProperty(key);
				if (strValue != null && logger.isInfoEnabled()) {
					logger.info("read from local config with key[" + key + "]:" + strValue);
				}
			} catch (Exception e) {
				logger.error("error while reading local config[" + key + "]:" + e.getMessage());
			}
		}
		if (strValue == null) {
			try {
				strValue = doGetProperty(key);
				if (strValue != null && logger.isInfoEnabled()) {
					logger.info("read from config server with key[" + key + "]:" + strValue);
				}
			} catch (Exception e) {
				logger.error("error while reading property[" + key + "]:" + e.getMessage());
			}
		}
		if (strValue != null) {
			Object value = null;
			if (String.class == type) {
				value = strValue;
			} else if (!StringUtils.isBlank(strValue)) {
				if (Integer.class == type) {
					value = Integer.valueOf(strValue);
				} else if (Long.class == type) {
					value = Long.valueOf(strValue);
				} else if (Float.class == type) {
					value = Float.valueOf(strValue);
				} else if (Boolean.class == type) {
					value = Boolean.valueOf(strValue);
				}
			}
			if (value != null) {
				localCache.put(key, value);
			}
			return (T) value;
		} else {
			logger.info("config[key=" + key + "] not found");
		}
		return null;
	}

	public int getLocalIntValue(String key, int defaultValue) {
		String strValue = getLocalProperty(key);
		if (!StringUtils.isBlank(strValue)) {
			return Integer.valueOf(strValue);
		}
		return defaultValue;
	}

	public long getLocalLongValue(String key, long defaultValue) {
		String strValue = getLocalProperty(key);
		if (!StringUtils.isBlank(strValue)) {
			return Long.valueOf(strValue);
		}
		return defaultValue;
	}

	public boolean getLocalBooleanValue(String key, boolean defaultValue) {
		String strValue = getLocalProperty(key);
		if (!StringUtils.isBlank(strValue)) {
			return Boolean.valueOf(strValue);
		}
		return defaultValue;
	}

	public String getLocalStringValue(String key, String defaultValue) {
		String value = getLocalProperty(key);
		return value != null ? value : defaultValue;
	}

	public String getLocalProperty(String key) {
		if (localCache.containsKey(key)) {
			String value = "" + localCache.get(key);
			if (logger.isInfoEnabled()) {
				logger.info("read from local cache with key[" + key + "]:" + value);
			}
			return value;
		}
		if (logger.isInfoEnabled()) {
			logger.info("try to read from local config with key[" + key + "]");
		}
		try {
			String value = doGetLocalProperty(key);
			if (value != null) {
				localCache.put(key, value);
				if (logger.isInfoEnabled()) {
					logger.info("read from config server with key[" + key + "]:" + value);
				}
				return value;
			} else {
				logger.info("config[key=" + key + "] not found in local config");
			}
		} catch (Exception e) {
			logger.error("error while reading property[" + key + "]:" + e.getMessage());
		}
		return null;
	}

	@Override
	public void init(Properties properties) {
		for (Iterator ir = properties.keySet().iterator(); ir.hasNext();) {
			String key = ir.next().toString();
			String value = properties.getProperty(key);
			localCache.put(key, value);
		}
	}

	public String getEnv() {
		String value = getLocalProperty(KEY_ENV);
		if (value == null) {
			try {
				value = doGetEnv();
			} catch (Exception e) {
				logger.error("error while reading env:" + e.getMessage());
			}
			if (value != null) {
				localCache.put(KEY_ENV, value);
			}
		}
		return value;
	}

	public String getAppName() {
		String value = getLocalProperty(KEY_APP_NAME);
		if (value == null) {
			try {
				value = doGetAppName();
			} catch (Exception e) {
				logger.error("error while reading app name:" + e.getMessage());
			}
			if (value != null) {
				localCache.put(KEY_APP_NAME, value);
			}
		}
		return value;
	}

	public String getLocalIp() {
		String value = getLocalProperty(KEY_LOCAL_IP);
		if (value == null) {
			try {
				value = doGetLocalIp();
			} catch (Exception e) {
				logger.error("error while reading local ip:" + e.getMessage());
			}
			if (StringUtils.isBlank(value)) {
				value = NetUtils.getFirstLocalIp();
			}
			if (value != null) {
				localCache.put(KEY_LOCAL_IP, value);
			}
		}
		return value;
	}

	public String getGroup() {
		String value = getLocalProperty(KEY_GROUP);
		if (value == null) {
			try {
				value = doGetGroup();
			} catch (Exception e) {
				logger.error("error while reading group:" + e.getMessage());
			}
			if (value != null) {
				localCache.put(KEY_GROUP, value);
			}
		}
		if (value == null) {
			return DEFAULT_GROUP;
		}
		return value;
	}

	public void registerConfigChangeListener(ConfigChangeListener configChangeListener) {
		configChangeListeners.add(configChangeListener);
	}

	@Override
	public void setStringValue(String key, String value) {
		try {
			doSetStringValue(key, value);
			setLocalStringValue(key, value);
		} catch (Exception e) {
			throw new ConfigException("error while setting key:" + key, e);
		}
	}

	@Override
	public void deleteKey(String key) {
		try {
			doDeleteKey(key);
			localCache.remove(key);
		} catch (Exception e) {
			throw new ConfigException("error while deleting key:" + key, e);
		}
	}

	@Override
	public void setLocalStringValue(String key, String value) {
		localCache.put(key, value);
		for (ConfigChangeListener listener : configChangeListeners) {
			listener.onChange(key, value);
		}
	}

	public Map<String, Object> getLocalConfig() {
		return localCache;
	}
}
