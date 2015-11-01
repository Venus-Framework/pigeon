/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.threadpool.RequestThreadPoolProcessor;

public class RequestProcessorFactory {

	public static RequestProcessor selectProcessor(ServerConfig serverConfig) {
		RequestProcessor requestProcessor = ExtensionLoader.getExtension(RequestProcessor.class);
		if (requestProcessor != null) {
			return requestProcessor;
		} else {
			return new RequestThreadPoolProcessor(serverConfig);
		}
	}
}
