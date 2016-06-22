/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.lion;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.dianping.lion.Environment;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.pigeon.config.AbstractConfigManager;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.LocalConfigLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.FileUtils;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class LionConfigManager extends AbstractConfigManager {

	private static Logger logger = LoggerLoader.getLogger(LionConfigManager.class);

	private ConfigCache configCache = null;

	private static final String ENV_FILE = "/data/webapps/appenv";

	private String env = null;
	private String group = null;
	private String ip = null;

	public LionConfigManager() {
		try {
			getConfigCache().addChange(new ConfigChange() {

				@Override
				public void onChange(String key, String value) {
					onConfigUpdated(key, value);
				}

			});
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private ConfigCache getConfigCache() throws LionException {
		init();
		return configCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.pigeon.config.ConfigManager#getProperty(java.lang.String)
	 */
	@Override
	public String doGetProperty(String key) throws Exception {
		return getConfigCache().getProperty(key);
	}

	public String getConfigServerAddress() {
		return EnvZooKeeperConfig.getZKAddress();
	}

	public String doGetEnv() throws Exception {
		String env = EnvZooKeeperConfig.getEnv();
		if (env != null) {
			return env.trim();
		}
		return env;
	}

	@Override
	public String doGetLocalIp() throws Exception {
		return ip;
	}

	@Override
	public String doGetGroup() throws Exception {
		return Environment.getSwimlane();
	}

	@Override
	public String doGetLocalProperty(String key) throws Exception {
		return null;
	}

	@Override
	public void doSetStringValue(String key, String value) {
		/*
		 * if (logger.isInfoEnabled()) { logger.info("set key[" + key + "]"); }
		 * 
		 * ZooKeeperWrapper zk; try { zk =
		 * ConfigCache.getInstance(getConfigServerAddress()).getZk();
		 * 
		 * if (zk.exists(key, false) == null) { String[] pathArray =
		 * key.split("/"); StringBuilder pathStr = new StringBuilder(); for (int
		 * i = 0; i < pathArray.length - 1; i++) { String path = pathArray[i];
		 * if (StringUtils.isNotBlank(path)) { pathStr.append("/").append(path);
		 * if (zk.exists(pathStr.toString(), false) == null) {
		 * zk.create(pathStr.toString(), new byte[0], Ids.OPEN_ACL_UNSAFE,
		 * CreateMode.PERSISTENT); } } } } byte[] bytes =
		 * value.getBytes("UTF-8"); if (zk.exists(key, false) == null) {
		 * zk.create(key, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); }
		 * else { zk.setData(key, bytes, -1); } } catch (Throwable e) { throw
		 * new ConfigException(e); }
		 */
	}

	@Override
	public void doDeleteKey(String key) throws Exception {
		/*
		 * ZooKeeperWrapper zk; zk =
		 * ConfigCache.getInstance(getConfigServerAddress()).getZk(); Stat
		 * statWeight = zk.exists(key, false); if (statWeight != null) {
		 * zk.delete(key, statWeight.getVersion()); }
		 */
	}

	@Override
	public void doRegisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception {
	}

	@Override
	public void init() {
		if (configCache == null) {
			synchronized (this) {
				if (configCache == null) {
					Map<String, Object> config = new HashMap<String, Object>();
					try {
						LocalConfigLoader.loadProperties(config, FileUtils.readFile(new FileInputStream(ENV_FILE)));
					} catch (Throwable e) {
					}
					ip = (String) config.get("ip");
					try {
						configCache = ConfigCache.getInstance();
					} catch (Exception e) {
						configCache = ConfigCache.getInstance(getConfigServerAddress());
					}
				}
			}
		}
	}

	public String toString() {
		return "LionConfigManager";
	}
}
