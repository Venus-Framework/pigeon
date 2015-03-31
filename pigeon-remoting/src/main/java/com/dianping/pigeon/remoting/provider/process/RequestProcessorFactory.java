/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.akka.RequestAkkaProcessor;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;

public class RequestProcessorFactory {

	private static final String processType = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.provider.processtype", "threadpool");

	public static RequestProcessor selectProcessor(ServerConfig serverConfig) {
		if ("akka".equalsIgnoreCase(processType)) {
			return new RequestAkkaProcessor(serverConfig);
		}
		return new RequestThreadPoolProcessor(serverConfig);
	}
}
