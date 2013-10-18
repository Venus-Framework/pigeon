/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.zookeeper.watcher;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.zookeeper.ZookeeperRegistry;

public class PigeonHostWatcher implements Watcher {

	private static final Logger logger = Logger.getLogger(PigeonHostWatcher.class);
	private ZookeeperRegistry pigeonCache;

	public PigeonHostWatcher(ZookeeperRegistry pigeonCache) {
		this.pigeonCache = pigeonCache;
	}

	@Override
	public void process(WatchedEvent event) {

		if (event.getType() == EventType.NodeDataChanged) {
			String keyPath = event.getPath();
			String key = keyPath.substring(keyPath.lastIndexOf("/") + 1);
			String weight = null;
			try {
				weight = pigeonCache.getWeightValue(key);
			} catch (RegistryException e) {
				logger.error(e.getMessage(), e);
			}
			if (pigeonCache.getHost().containsKey(key)) {
				pigeonCache.getServiceChangeListener().onHostWeightChange(key, Integer.parseInt(weight));
			}
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Host Node " + event.getType() + " " + event.getPath());
			}
			String keyPath = event.getPath();
			String key = keyPath.substring(keyPath.lastIndexOf("/") + 1);
			try {
				pigeonCache.getWeightValue(key);
			} catch (RegistryException e) {
				logger.error("", e);
			}
		}
	}
}
