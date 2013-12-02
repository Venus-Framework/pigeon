/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.akka.RequestAkkaProcessor;
import com.dianping.pigeon.remoting.provider.process.disruptor.RequestDisruptorProcessor;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;

public class RequestProcessorFactory {

	public static RequestProcessor selectProcessor(ServerConfig serverConfig) {
		String processType = serverConfig.getProcessType();
		if ("akka".equalsIgnoreCase(processType)) {
			return new RequestAkkaProcessor(serverConfig);
		} else if ("disruptor".equalsIgnoreCase(processType)) {
			return new RequestDisruptorProcessor(serverConfig);
		}
		return new RequestThreadPoolProcessor(serverConfig);
	}
}
