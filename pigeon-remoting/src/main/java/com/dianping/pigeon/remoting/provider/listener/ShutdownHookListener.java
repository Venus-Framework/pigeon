/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.loader.ProviderBootStrapLoader;

public class ShutdownHookListener implements Runnable {

	static final Logger logger = LoggerLoader.getLogger(ProviderBootStrapLoader.class);
	Server server = null;

	public ShutdownHookListener(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		if (logger.isInfoEnabled()) {
			logger.info("shutdown hook begin......");
		}
		if (server != null) {
			try {
				ServiceFactory.unpublishAllServices();
			} catch (RpcException e) {
				logger.error("error with shutdown hook", e);
			}
			ProviderBootStrapLoader.shutdown();
		}
		if (logger.isInfoEnabled()) {
			logger.info("shutdown hook end......");
		}
	}

}
