/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public class ServiceOnlineListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ServiceOnlineListener.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final int CHECK_INTERVAL = configManager.getIntValue(Constants.KEY_WEIGHT_CHECKINTERVAL,
			Constants.DEFAULT_WEIGHT_CHECKINTERVAL);

	private static final int START_DELAY = configManager.getIntValue(Constants.KEY_WEIGHT_STARTDELAY, CHECK_INTERVAL);

	public void run() {
		try {
			Thread.sleep(START_DELAY);
			ServiceProviderFactory.setServerOnline();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(CHECK_INTERVAL);
				ServiceProviderFactory.setServerOnline();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
