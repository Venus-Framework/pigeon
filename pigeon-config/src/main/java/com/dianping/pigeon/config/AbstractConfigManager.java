/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.AppUtils;
import com.dianping.pigeon.util.NetUtils;

/**
 * @author xiangwu
 * @Sep 22, 2013
 *
 */
public abstract class AbstractConfigManager implements ConfigManager {

	private static Logger logger = LoggerLoader.getLogger(AbstractConfigManager.class);

	public static final String KEY_GROUP = "swimlane";

	public static final String KEY_LOCAL_IP = "host.ip";

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_ENV = "environment";

	public static final String DEFAULT_GROUP = "";

	public static final int DEFAULT_WEIGHT = 1;

	private static final Object NULL = new Object();

	private static List<ConfigChangeListener> configChangeListeners = new ArrayList<ConfigChangeListener>();

	protected Map<String, Object> localCache = new ConcurrentHashMap<String, Object>();

	public abstract String doGetProperty(String key) throws Exception;

	public abstract String doGetLocalProperty(String key) throws Exception;

	public abstract String doGetEnv() throws Exception;

	public abstract String doGetLocalIp() throws Exception;

	public abstract String doGetGroup() throws Exception;

	public abstract void doSetStringValue(String key, String value) throws Exception;

	public abstract void doDeleteKey(String key) throws Exception;

	public AbstractConfigManager() {
		Map<String, Object> properties = LocalConfigLoader.load(this);
		localCache.putAll(properties);
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

	public double getDoubleValue(String key, double defaultValue) {
		Double value = getDoubleValue(key);
		return value != null ? value : defaultValue;
	}

	public Double getDoubleValue(String key) {
		return getProperty(key, Double.class);
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
			if (value == NULL) {
				return null;
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
			strValue = System.getenv(key);
		}
		if (strValue == null) {
			try {
				strValue = doGetLocalProperty(key);
			} catch (Throwable e) {
				logger.error("error while reading local config[" + key + "]:" + e.getMessage());
			}
		}
		Object value = null;
		if (strValue != null) {
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
		}
		setLocalValue(key, value);
		return (T) value;
	}

	@Override
	public String getStringValue(String key) {
		return getProperty(key, String.class);
	}

	private <T> T getProperty(String key, Class<T> type) {
		String strValue = null;
		if (localCache.containsKey(key)) {
			Object value = localCache.get(key);
			if (value == NULL) {
				return null;
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
			strValue = System.getenv(key);
		}
		if (strValue == null) {
			try {
				strValue = doGetLocalProperty(key);
			} catch (Throwable e) {
				logger.error("error while reading local config[" + key + "]:" + e.getMessage());
			}
		}
		if (strValue == null && StringUtils.isNotBlank(getAppName())) {
			if (!key.startsWith(getAppName())) {
				try {
					strValue = doGetProperty(getAppName() + "." + key);
					if (strValue != null && logger.isInfoEnabled()) {
						logger.info("read from config server with key[" + getAppName() + "." + key + "]:" + strValue);
					}
				} catch (Throwable e) {
					logger.error("error while reading property[" + getAppName() + "." + key + "]:" + e.getMessage());
				}
			}
		}
		if (strValue == null) {
			try {
				strValue = doGetProperty(key);
				if (strValue != null && logger.isInfoEnabled()) {
					logger.info("read from config server with key[" + key + "]:" + strValue);
				}
			} catch (Throwable e) {
				logger.error("error while reading property[" + key + "]:" + e.getMessage());
			}
		}
		Object value = null;
		if (strValue != null) {
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
				} else if (Double.class == type) {
					value = Double.valueOf(strValue);
				}
			}
		}
		setLocalValue(key, value);
		return (T) value;
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
			Object v = localCache.get(key);
			if (v == NULL) {
				return "";
			} else {
				return "" + v;
			}
		}
		try {
			String value = doGetLocalProperty(key);
			setLocalValue(key, value);
			if (value != null) {
				if (logger.isInfoEnabled()) {
					logger.info("read from config server with key[" + key + "]:" + value);
				}
				return value;
			}
		} catch (Throwable e) {
			logger.error("error while reading property[" + key + "]:" + e.getMessage());
		}
		return null;
	}

	@Override
	public void init(Properties properties) {
		for (Iterator ir = properties.keySet().iterator(); ir.hasNext();) {
			String key = ir.next().toString();
			String value = properties.getProperty(key);
			setLocalValue(key, value);
		}
	}

	public String getEnv() {
		String value = getLocalProperty(KEY_ENV);
		if (value == null) {
			try {
				value = doGetEnv();
			} catch (Throwable e) {
				logger.error("error while reading env:" + e.getMessage());
			}
			if (value != null) {
				setLocalValue(KEY_ENV, value);
				logger.info("environment:" + value);
			}
		}
		return value;
	}

	public String getAppName() {
		String value = getLocalProperty(KEY_APP_NAME);
		if (value == null) {
			try {
				value = AppUtils.getAppName();
			} catch (Throwable e) {
				logger.error("error while reading app name:" + e.getMessage());
			}
			if (value != null) {
				setLocalValue(KEY_APP_NAME, value);
			}
			if (StringUtils.isNotBlank(value)) {
				logger.info("app name:" + value);
			}
		}
		return value;
	}

	public String getLocalIp() {
		String value = null;
		try {
			value = doGetLocalIp();
		} catch (Throwable e) {
			logger.error("error while reading local ip:" + e.getMessage());
		}
		if (StringUtils.isBlank(value)) {
			value = NetUtils.getFirstLocalIp();
		}
		if (value != null) {
			setLocalValue(KEY_LOCAL_IP, value);
		}
		return value;
	}

	public String getGroup() {
		String value = null;
		try {
			value = doGetGroup();
		} catch (Throwable e) {
			logger.error("error while reading group:" + e.getMessage());
		}
		if (value == null) {
			return DEFAULT_GROUP;
		}
		return value;
	}

	public void registerConfigChangeListener(ConfigChangeListener configChangeListener) {
		configChangeListeners.add(configChangeListener);
		try {
			doRegisterConfigChangeListener(configChangeListener);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public abstract void doRegisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception;

	@Override
	public void setStringValue(String key, String value) {
		try {
			doSetStringValue(key, value);
			setLocalStringValue(key, value);
		} catch (Throwable e) {
			throw new ConfigException("error while setting key:" + key, e);
		}
	}

	@Override
	public void deleteKey(String key) {
		try {
			doDeleteKey(key);
			localCache.remove(key);
		} catch (Throwable e) {
			throw new ConfigException("error while deleting key:" + key, e);
		}
	}

	@Override
	public void setLocalStringValue(String key, String value) {
		setLocalValue(key, value);
	}

	public void setLocalValue(String key, Object value) {
		if (key != null) {
			if (value != null) {
				localCache.put(key, value);
			} else {
				localCache.put(key, NULL);
			}
		}
	}

	public Map<String, Object> getLocalConfig() {
		return localCache;
	}

	public List<ConfigChangeListener> getConfigChangeListeners() {
		return configChangeListeners;
	}

	public void onConfigUpdated(String key, String value) {
		List<ConfigChangeListener> listeners = getConfigChangeListeners();
		for (ConfigChangeListener listener : listeners) {
			listener.onKeyUpdated(key, value);
		}
		if (localCache.containsKey(key)) {
			setLocalValue(key, value);
		}
	}

	public void onConfigAdded(String key, String value) {
		List<ConfigChangeListener> listeners = getConfigChangeListeners();
		for (ConfigChangeListener listener : listeners) {
			listener.onKeyAdded(key, value);
		}
	}

	public void onConfigRemoved(String key, String value) {
		List<ConfigChangeListener> listeners = getConfigChangeListeners();
		for (ConfigChangeListener listener : listeners) {
			listener.onKeyRemoved(key);
		}
	}

}
