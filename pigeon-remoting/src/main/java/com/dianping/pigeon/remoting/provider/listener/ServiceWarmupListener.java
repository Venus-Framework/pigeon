/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public class ServiceWarmupListener implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ServiceWarmupListener.class);

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static final int CHECK_INTERVAL = configManager.getIntValue(Constants.KEY_WEIGHT_WARMUPPERIOD,
			Constants.DEFAULT_WEIGHT_WAMUPPERIOD);

	private static volatile boolean isServiceWarmupListenerStarted = false;

	private static ServiceWarmupListener currentWarmupListener = null;

	private volatile boolean isStop = false;

	private int delay = configManager.getIntValue(Constants.KEY_WEIGHT_STARTDELAY, CHECK_INTERVAL);

	public static void start() {
		start(-1);
	}

	public static void start(int delay) {
		boolean warmupEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
				Constants.KEY_SERVICEWARMUP_ENABLE, true);
		if (!isServiceWarmupListenerStarted && warmupEnable && ServiceProviderFactory.isAutoPublish()) {
			currentWarmupListener = new ServiceWarmupListener(delay);
			Thread t = new Thread(currentWarmupListener);
			t.setDaemon(true);
			t.setName("Pigeon-Service-Warmup-Listener");
			t.start();
			isServiceWarmupListenerStarted = true;
		}
	}

	public static void stop() {
		if (currentWarmupListener != null) {
			currentWarmupListener.setStop(true);
			while (isServiceWarmupListenerStarted) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			currentWarmupListener = null;
		}
	}

	public ServiceWarmupListener(int delay) {
		if (delay >= 0) {
			this.delay = delay;
		}
	}

	public boolean isStop() {
		return isStop;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	public void run() {
		try {
			Thread.sleep(delay);
			if (!isStop) {
				ServiceFactory.online();
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}

		logger.info("Warm-up task end, current weight:" + ServiceProviderFactory.getServerWeight());
		isServiceWarmupListenerStarted = false;
	}
}
