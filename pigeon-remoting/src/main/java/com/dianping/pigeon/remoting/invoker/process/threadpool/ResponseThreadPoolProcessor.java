/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RejectedException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.process.AbstractResponseProcessor;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ResponseThreadPoolProcessor extends AbstractResponseProcessor {

	private static ThreadPool responseProcessThreadPool;
	private static ClientManager clientManager = ClientManager.getInstance();

	public ResponseThreadPoolProcessor() {
		ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);
		int corePoolSize = configManager.getIntValue(Constants.KEY_RESPONSE_COREPOOLSIZE,
				Constants.DEFAULT_RESPONSE_COREPOOLSIZE);
		int maxPoolSize = configManager.getIntValue(Constants.KEY_RESPONSE_MAXPOOLSIZE,
				Constants.DEFAULT_RESPONSE_MAXPOOLSIZE);
		int queueSize = configManager.getIntValue(Constants.KEY_RESPONSE_WORKQUEUESIZE,
				Constants.DEFAULT_RESPONSE_WORKQUEUESIZE);
		responseProcessThreadPool = new DefaultThreadPool("Pigeon-Client-Response-Processor", corePoolSize,
				maxPoolSize, new LinkedBlockingQueue<Runnable>(queueSize), new CallerRunsPolicy());
	}

	public void stop() {
	}

	public void doProcessResponse(final InvocationResponse response, final Client client) {
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
		} catch (RejectedExecutionException e) {
			String error = String.format("process response failed:%s, processor stats:%s", response,
					getProcessorStatistics());
			throw new RejectedException(error, e);
		}
	}

	@Override
	public String getProcessorStatistics() {
		ThreadPoolExecutor e = responseProcessThreadPool.getExecutor();
		String stats = String.format(
				"response pool size:%d(active:%d,core:%d,max:%d,largest:%),task count:%d(completed:%d),queue size:%d",
				e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
				e.getLargestPoolSize(), e.getTaskCount(), e.getCompletedTaskCount(), e.getQueue().size());
		return stats;
	}
}
