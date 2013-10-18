/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.cache;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.QueryString;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.config.DefaultRegistryConfigManager;
import com.dianping.pigeon.registry.config.RegistryConfigManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.ConfigChangeListener;
import com.dianping.pigeon.registry.util.Constants;

/**
 * 
 * 
 * @author jianhuihuang, saber miao
 * @version $Id: ConfigCache.java, v 0.1 2013-6-20 下午10:14:31 jianhuihuang Exp $
 */
public class RegistryCache {

	private static final Logger logger = Logger.getLogger(RegistryCache.class);

	private static final String avatarBizPrefix = "avatar-biz.";

	private static Properties defaultPts = new Properties();

	private static boolean isLocal = false;

	private static boolean isInit = false;

	private static Registry registry = ExtensionLoader.getExtension(Registry.class);

	private static RegistryConfigManager registryConfigManager = new DefaultRegistryConfigManager();

	private static RegistryCache registryCache = new RegistryCache();

	private RegistryCache() {

	}

	public static RegistryCache getInstance() {
		if (!isInit) {
			registryCache.init(registryConfigManager.getRegistryConfig());
			isInit = true;
		}
		return registryCache;
	}

	public void init(Properties properties) {
		registryCache.setPts(properties);
		String registryType = properties.getProperty("registryType");
		if (!"local".equalsIgnoreCase(registryType)) {
			if (registry != null) {
				registry.init(properties);
			}
		} else {
			isLocal = true;
		}
	}

	public static String getProperty(String key) throws RegistryException {
		if (key != null) {
			key = key.trim();
		} else {
			return null;
		}
		if (Constants.avatarBizKeySet.contains(key)) {
			key = avatarBizPrefix + key;
		}
		String v = defaultPts.getProperty(key);
		if (v != null && !v.startsWith("${") && !v.endsWith("}")) {
			return v;
		}
		if (v != null && v.startsWith("${") && v.endsWith("}")) {
			v = v.substring(2);
			v = v.substring(0, v.length() - 1);
			return getValueFromRegistry(v);
		}

		if (v == null) {
			v = getValueFromRegistry(key);
		}
		return v;
	}

	private static String getValueFromRegistry(String key) throws RegistryException {
		if (!isLocal) {
			return registry.getValue(key);
		}
		return null;
	}

	public static Long getLongProperty(String key) throws RegistryException {
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		return Long.parseLong(value);
	}

	public static Integer getIntProperty(String key) throws RegistryException {
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		return Integer.parseInt(value);
	}

	public static Boolean getBooleanProperty(String key) throws RegistryException {
		String value = getProperty(key);
		if (value == null) {
			return null;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * @param pts
	 *            the pts to set
	 */
	public static void update(String key, String value) {
		// 如果是dev环境，可以把当前配置加载进去
		defaultPts.put(key, value);
	}

	/**
	 * @param pts
	 *            the pts to set
	 */
	public void setPts(Properties pts) {
		defaultPts.putAll(pts);
	}

	public void addConfigChangeListener(ConfigChangeListener configChangeListener) {
		if (registry != null) {
			registry.addConfigChangeListener(configChangeListener);
		}

	}

	public String getServiceAddress(String serviceName) throws RegistryException {
		String address = _getServiceAddress(serviceName);
		if (address != null) {
			return address;
		}
		// 处理zone和group
		String[] parts = serviceName.split(QueryString.PREFIX_REGEXP, 2);
		if (parts.length > 1) {
			QueryString qs = new QueryString(parts[1]);
			String zone = qs.getParameter("zone");
			String group = qs.getParameter("group");
			if (zone != null && group != null) {
				// TODO 先缺省zone还是先缺省group?
				address = _getServiceAddress(parts[0] + QueryString.PREFIX
						+ new QueryString().addParameter("zone", zone));
				if (address == null)
					address = _getServiceAddress(parts[0] + QueryString.PREFIX
							+ new QueryString().addParameter("group", group));
			}
			if (address == null) {
				address = _getServiceAddress(parts[0]);
			}
		}
		return address;
	}

	private String _getServiceAddress(String serviceName) throws RegistryException {
		if (defaultPts.containsKey(serviceName)) {
			if (logger.isInfoEnabled()) {
				logger.info("Pigeon Get Pigeon Service From Application Context! Key:" + serviceName + "  Value:"
						+ defaultPts.getProperty(serviceName));
			}
			return defaultPts.getProperty(serviceName);
		}
		if (registry != null) {
			return registry.getServiceAddress(serviceName);
		}

		return null;
	}

	public Integer getServiceWeigth(String serviceAddress) throws RegistryException {
		if (registry != null) {
			registry.getServiceWeigth(serviceAddress);
		}
		return 1;
	}

	public void publishService(String serviceName, String serviceAddress) throws RegistryException {
		if (registry != null) {
			registry.publishServiceAddress(serviceName, serviceAddress);
		}
	}

}
