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
import com.dianping.pigeon.remoting.provider.service.PublishStatus;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public class ServiceWarmupListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ServiceWarmupListener.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static final int CHECK_INTERVAL = configManager.getIntValue(Constants.KEY_WEIGHT_CHECKINTERVAL,
			Constants.DEFAULT_WEIGHT_CHECKINTERVAL);

	private static final int START_DELAY = configManager.getIntValue(Constants.KEY_WEIGHT_STARTDELAY, CHECK_INTERVAL);

	private static final int WEIGHT_DEFAULT = configManager.getIntValue(Constants.KEY_WEIGHT_DEFAULT,
			Constants.DEFAULT_WEIGHT_DEFAULT);

	private static final int WEIGHT_MAX = configManager.getIntValue(Constants.KEY_WEIGHT_MAX,
			Constants.DEFAULT_WEIGHT_MAX);

	private static volatile boolean isServiceWarmupListenerStarted = false;

	public static void start() {
		if (!isServiceWarmupListenerStarted) {
			new Thread(new ServiceWarmupListener()).start();
			isServiceWarmupListenerStarted = true;
		}
	}

	public void run() {
		try {
			Thread.sleep(START_DELAY);
			ServiceProviderFactory.setServerWeight(WEIGHT_DEFAULT);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		ServiceProviderFactory.setPublishStatus(PublishStatus.PUBLISHED);
		int weight = WEIGHT_DEFAULT;
		while (weight < WEIGHT_MAX) {
			ServiceProviderFactory.setPublishStatus(PublishStatus.WARMINGUP);
			try {
				Thread.sleep(CHECK_INTERVAL);
				if (!ServiceProviderFactory.getPublishStatus().equals(PublishStatus.WARMINGUP)
						&& !ServiceProviderFactory.getPublishStatus().equals(PublishStatus.PUBLISHED)) {
					logger.warn("Warm-up task will be end, current status:" + ServiceProviderFactory.getPublishStatus());
					break;
				}
				ServiceProviderFactory.setServerWeight(++weight);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		if (weight == WEIGHT_MAX) {
			ServiceProviderFactory.setPublishStatus(PublishStatus.WARMEDUP);
		}
		logger.info("Warm-up task end, current weight:" + weight);
		isServiceWarmupListenerStarted = false;
	}
}
