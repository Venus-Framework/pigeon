/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.publish;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;

public class ServiceOnlineTask implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(ServiceOnlineTask.class);

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static final int CHECK_INTERVAL = configManager.getIntValue("pigeon.online.task.interval", 1000);

	private static volatile boolean isServiceOnlineTaskStarted = false;

	private static ServiceOnlineTask serviceOnlineTask = null;

	private volatile boolean isStop = false;

	private int delay = configManager.getIntValue("pigeon.online.task.startdelay", CHECK_INTERVAL);

	public static void start() {
		start(-1);
	}

	public static void start(int delay) {
		boolean enableOnlineTask = ConfigManagerLoader.getConfigManager()
				.getBooleanValue("pigeon.online.task.enable", true);
		if (!isServiceOnlineTaskStarted && enableOnlineTask && ServicePublisher.isAutoPublish()) {
			serviceOnlineTask = new ServiceOnlineTask(delay);
			logger.info("Service online task is enabled");
			Thread t = new Thread(serviceOnlineTask);
			t.setDaemon(true);
			t.setName("Pigeon-Service-Online-Task");
			t.start();
			isServiceOnlineTaskStarted = true;
		} else {
			logger.info("Service online task is disabled");
		}
	}

	public static void stop() {
		if (serviceOnlineTask != null) {
			serviceOnlineTask.setStop(true);
			while (isServiceOnlineTaskStarted) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			serviceOnlineTask = null;
		}
	}

	public ServiceOnlineTask(int delay) {
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
		logger.info("Service online task start");
		try {
			Thread.sleep(delay);
			if (!isStop) {
				ServiceFactory.online();
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}

		logger.info("Service online task end, current weight:" + ServicePublisher.getServerWeight());
		isServiceOnlineTaskStarted = false;
	}
}
