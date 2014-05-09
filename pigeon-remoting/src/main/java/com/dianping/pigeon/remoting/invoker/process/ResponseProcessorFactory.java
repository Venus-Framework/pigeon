/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.remoting.invoker.process.threadpool.ResponseThreadPoolProcessor;

public class ResponseProcessorFactory {

	private static ResponseProcessor responseProcessor = new ResponseThreadPoolProcessor();
	
	public static ResponseProcessor selectProcessor() {
		// ConfigManager configManager =
		// ExtensionLoader.getExtension(ConfigManager.class);
		// String processType =
		// configManager.getStringValue(Constants.KEY_PROCESS_TYPE,
		// Constants.DEFAULT_PROCESS_TYPE);
		return responseProcessor;
	}
}
