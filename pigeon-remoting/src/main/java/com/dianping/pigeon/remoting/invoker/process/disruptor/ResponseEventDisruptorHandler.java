/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.disruptor;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.process.event.ResponseEvent;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.lmax.disruptor.EventHandler;

public class ResponseEventDisruptorHandler implements EventHandler<ResponseEvent> {

	private static final Logger logger = LoggerLoader.getLogger(ResponseEventDisruptorHandler.class);
	private static ThreadPool responseProcessThreadPool = new DefaultThreadPool("Pigeon-client-Response-Processor-Disruptor", 10,
			200, new LinkedBlockingQueue<Runnable>(100));
	private ClientManager clientManager = ClientManager.getInstance();

	public void onEvent(final ResponseEvent event, long sequence, boolean endOfBatch) throws Exception {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				if (event.getResponse().getMessageType() == Constants.MESSAGE_TYPE_HEART) {
					clientManager.getHeartTask().processResponse(event.getResponse(), event.getClient());
				} else {
					ServiceInvocationRepository.getInstance().receiveResponse(event.getResponse());
				}
			}
		};
		responseProcessThreadPool.submit(task);
	}
}
