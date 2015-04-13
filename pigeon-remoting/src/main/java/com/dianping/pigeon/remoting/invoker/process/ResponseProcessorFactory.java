/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.invoker.process.actor.ResponseActorProcessor;
import com.dianping.pigeon.remoting.invoker.process.threadpool.ResponseThreadPoolProcessor;

public class ResponseProcessorFactory {

	private static final String processType = ConfigManagerLoader.getConfigManager().getStringValue(
			"pigeon.invoker.processtype", "thread");

	private static ResponseProcessor responseProcessor = null;

	public static ResponseProcessor selectProcessor() {
		if ("actor".equals(processType)) {
			responseProcessor = new ResponseActorProcessor();
		} else {
			responseProcessor = new ResponseThreadPoolProcessor();
		}
		return responseProcessor;
	}

	public static void stop() {
		if (responseProcessor != null) {
			responseProcessor.stop();
		}
	}
}
