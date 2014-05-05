/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.util.Map;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public class ServiceRegistryListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ServiceRegistryListener.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final int CHECK_INTERVAL = configManager.getIntValue(Constants.KEY_WEIGHT_CHECKINTERVAL,
			Constants.DEFAULT_WEIGHT_CHECKINTERVAL);

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(CHECK_INTERVAL);
				Map<String, Integer> serverWeight = ServiceProviderFactory.getServerWeight();
				for (String serverAddress : serverWeight.keySet()) {
					int weight = serverWeight.get(serverAddress);
					if (weight == 0) {
						int newWeight = Constants.DEFAULT_WEIGHT;
						if (logger.isInfoEnabled()) {
							logger.info("set weight, address:" + serverAddress + ", weight:" + newWeight);
						}
						RegistryManager.getInstance().setServerWeight(serverAddress, newWeight);
						serverWeight.put(serverAddress, newWeight);
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
