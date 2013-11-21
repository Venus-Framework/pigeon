/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class ResponseProcessor {

	private static final Logger logger = LoggerLoader.getLogger(ResponseProcessor.class);

	private static ThreadPool responseProcessThreadPool = new DefaultThreadPool(
			Constants.THREADNAME_CLIENT_PRESPONSE_PROCESSOR, 20, 300, new LinkedBlockingQueue<Runnable>(50),
			new CallerRunsPolicy());

	private ClientManager clientManager = ClientManager.getInstance();

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
			// [v1.7.0, danson.liu]对于callback调用, 防止callback阻塞response
			// handler thread pool线程池, 影响其他正常响应无法处理
			responseProcessThreadPool.execute(task);
		} catch (Exception ex) {
			String msg = "Response execute fail:seq--" + response.getSequence() + "\r\n";
			logger.error(msg + ex.getMessage(), ex);
		}
	}

}
