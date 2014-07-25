/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.config.lion;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ZooKeeperWrapper;
import com.dianping.pigeon.config.AbstractConfigManager;
import com.dianping.pigeon.config.ConfigException;
import com.dianping.pigeon.log.LoggerLoader;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class LionConfigManager extends AbstractConfigManager {

	private static Logger logger = LoggerLoader.getLogger(LionConfigManager.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.pigeon.config.ConfigManager#getProperty(java.lang.String)
	 */
	@Override
	public String doGetProperty(String key) throws Exception {
		if (logger.isInfoEnabled()) {
			// logger.info("read from lion config with key[" + key + "]");
		}
		return ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty(key);
	}

	public String getConfigServerAddress() {
		return EnvZooKeeperConfig.getZKAddress();
	}

	public String doGetEnv() throws Exception {
		return EnvZooKeeperConfig.getEnv();
	}

	@Override
	public String doGetAppName() throws Exception {
		return "";
	}

	@Override
	public String doGetLocalIp() throws Exception {
		return null;
	}

	@Override
	public String doGetGroup() throws Exception {
		return ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getAppenv(KEY_GROUP);
	}

	@Override
	public String doGetLocalProperty(String key) throws Exception {
		return null;
	}

	@Override
	public void doSetStringValue(String key, String value) {
		if (logger.isInfoEnabled()) {
			logger.info("set key[" + key + "]");
		}
		ZooKeeperWrapper zk;
		try {
			zk = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getZk();

			if (zk.exists(key, false) == null) {
				String[] pathArray = key.split("/");
				StringBuilder pathStr = new StringBuilder();
				for (int i = 0; i < pathArray.length - 1; i++) {
					String path = pathArray[i];
					if (StringUtils.isNotBlank(path)) {
						pathStr.append("/").append(path);
						if (zk.exists(pathStr.toString(), false) == null) {
							zk.create(pathStr.toString(), new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}
					}
				}
			}
			byte[] bytes = value.getBytes("UTF-8");
			if (zk.exists(key, false) == null) {
				zk.create(key, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} else {
				zk.setData(key, bytes, -1);
			}
		} catch (Throwable e) {
			throw new ConfigException(e);
		}
	}

	@Override
	public void doDeleteKey(String key) throws Exception {
		ZooKeeperWrapper zk;
		zk = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getZk();
		Stat statWeight = zk.exists(key, false);
		if (statWeight != null) {
			zk.delete(key, statWeight.getVersion());
		}
	}

}
