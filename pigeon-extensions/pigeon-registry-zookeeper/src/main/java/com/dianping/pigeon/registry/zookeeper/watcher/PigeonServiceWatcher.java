/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.zookeeper.watcher;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Constants;
import com.dianping.pigeon.registry.zookeeper.ZookeeperRegistry;

public class PigeonServiceWatcher implements Watcher {

	private static final Logger logger = Logger.getLogger(PigeonServiceWatcher.class);
	private ZookeeperRegistry pigeonCache;

	public PigeonServiceWatcher(ZookeeperRegistry pigeonCache) {
		this.pigeonCache = pigeonCache;
	}

	private String replaceServiceName(String temp) {
		return temp.replace(Constants.PLACEHOLD, "/");
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == EventType.NodeDataChanged) {
			String keyPath = event.getPath();
			String key = keyPath.substring(keyPath.lastIndexOf("/") + 1);
			String value = "";
			try {
				value = pigeonCache.getServiceValue(key);
			} catch (RegistryException e) {
				logger.error(e.getMessage(), e);
			}
			if (logger.isInfoEnabled()) {
				logger.info("Pigeon Cache Key Change! Key: " + key + " Value: " + value);
			}
			if (pigeonCache.getService().containsKey(key)) {
				List<String[]> hostDetail = pigeonCache.getServiceIpPortWeight(value);
				pigeonCache.getServiceChangeListener().onServiceHostChange(this.replaceServiceName(key), hostDetail);
			}
		} else {
			if (event.getPath() != null && event.getPath().lastIndexOf("/") != -1) {
				// 重新监听节点信息
				if (logger.isInfoEnabled()) {
					logger.info("Delete The Node " + event.getType() + " " + event.getPath());
				}
				String key = event.getPath().substring(event.getPath().lastIndexOf("/") + 1);
				try {
					pigeonCache.getServiceValue(key);
				} catch (RegistryException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.error("Event type:" + event.getType() + ",state:" + event.getState());
			}
		}
	}
}
