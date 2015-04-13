/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.actor.RequestActorProcessor;

public class RequestProcessorFactory {

	private static final String processType = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.provider.processtype", "thread");

	public static RequestProcessor selectProcessor(ServerConfig serverConfig) {
		if ("actor".equalsIgnoreCase(processType)) {
			return new RequestActorProcessor(serverConfig);
		}
		return new RequestDefaultProcessor(serverConfig);
	}
}
