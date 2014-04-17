/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.pigeon.util.NetUtils;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public abstract class AbstractConfigManager implements ConfigManager {

	private static Logger logger = Logger.getLogger(AbstractConfigManager.class);

	public static final String KEY_GROUP = "swimlane";

	public static final String KEY_AUTO_REGISTER = "auto.register";

	public static final String KEY_LOCAL_IP = "host.ip";

	public static final String KEY_APP_NAME = "app.name";

	public static final String KEY_ENV = "environment";

	public static final String DEFAULT_GROUP = "";

	private static final String PROPERTIES_PATH = "config/applicationContext.properties";

	protected Map<String, Object> localCache = new HashMap<String, Object>();

	public abstract String doGetProperty(String key) throws Exception;

	public abstract String doGetLocalProperty(String key) throws Exception;

	public abstract String doGetEnv() throws Exception;

	public abstract String doGetLocalIp() throws Exception;

	public abstract String doGetAppName() throws Exception;

	public abstract String doGetGroup() throws Exception;

	public AbstractConfigManager() {
		if (ConfigConstants.ENV_DEV.equalsIgnoreCase(getEnv())) {
			try {
				init(readLocalConfig());
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	private Properties readLocalConfig() {
		Properties properties = new Properties();
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_PATH);
		BufferedReader br = null;
		if (input != null) {
			try {
				br = new BufferedReader(new InputStreamReader(input));
				String line = null;
				while ((line = br.readLine()) != null) {
					int idx = line.indexOf("=");
					if (idx != -1) {
						String key = line.substring(0, idx);
						String value = line.substring(idx + 1);
						properties.put(key.trim(), value.trim());
					}
				}
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return properties;
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

	@Override
	public String getStringValue(String key, String defaultValue) {
		String value = getStringValue(key);
		return value != null ? value : defaultValue;
	}

	public String getStringValueFromLocal(String key) {
		return getPropertyFromLocal(key, String.class);
	}

	private <T> T getPropertyFromLocal(String key, Class<T> type) {
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
		return value;
	}

}
