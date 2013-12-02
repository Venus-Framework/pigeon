/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ResponseThreadPoolProcessor implements ResponseProcessor {

	private static final Logger logger = LoggerLoader.getLogger(ResponseThreadPoolProcessor.class);
	private static ThreadPool responseProcessThreadPool;
	private ClientManager clientManager = ClientManager.getInstance();

	public ResponseThreadPoolProcessor() {
		ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
		int maxPoolSize = configManager.getIntValue(Constants.KEY_INVOKER_MAXPOOLSIZE,
				Constants.DEFAULT_INVOKER_MAXPOOLSIZE);
		responseProcessThreadPool = new DefaultThreadPool("Pigeon-Client-Response-Processor", 20, maxPoolSize,
				new LinkedBlockingQueue<Runnable>(50), new CallerRunsPolicy());
	}

	public void stop() {
	}

	public void processResponse(final InvocationResponse response, final Client client) {
		Runnable task = new Runnable() {
			public void run() {
				if (response.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
					clientManager.getHeartTask().processResponse(response, client);
				} else {
					ServiceInvocationRepository.getInstance().receiveResponse(response);
				}
			}
		};
		try {
			responseProcessThreadPool.execute(task);
		} catch (Exception ex) {
			String msg = "Response execute fail:seq--" + response.getSequence() + "\r\n";
			logger.error(msg + ex.getMessage(), ex);
		}
	}

}
