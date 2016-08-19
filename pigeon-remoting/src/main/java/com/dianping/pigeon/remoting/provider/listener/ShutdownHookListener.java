/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;

public class ShutdownHookListener implements Runnable {

	static final Logger logger = LoggerLoader.getLogger(ProviderBootStrap.class);

	public ShutdownHookListener() {
	}

	@Override
	public void run() {
		if (logger.isInfoEnabled()) {
			logger.info("shutdown hook begin......");
		}

		boolean isRocketShutdown = ConfigManagerLoader.getConfigManager().getBooleanValue("pigeon.invoker.rocketshutdown",false);
		if(isRocketShutdown && ServicePublisher.getAllServiceProviders().size() == 0){
			// rocket shutdown
		} else {
			try {
				ServiceFactory.unpublishAllServices();
			} catch (Throwable e) {
				logger.error("error with shutdown hook", e);
			}
			try {
				InvokerBootStrap.shutdown();
			} catch (Throwable e) {
				logger.error("error with shutdown hook", e);
			}
			try {
				ProviderBootStrap.shutdown();
			} catch (Throwable e) {
				logger.error("error with shutdown hook", e);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("shutdown hook end......");
		}
	}

}
