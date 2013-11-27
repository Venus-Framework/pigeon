/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.process.event.ResponseEvent;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;

public class ResponseProcessor {

	private static final Logger logger = LoggerLoader.getLogger(ResponseProcessor.class);
	private RingBuffer<ResponseEvent> ringBuffer;
	private SequenceBarrier sequenceBarrier;
	private ResponseEventHandler handler;
	private BatchEventProcessor<ResponseEvent> batchEventProcessor;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ResponseProcessor() {
		int maxPoolSize = configManager.getIntValue(Constants.KEY_INVOKER_MAXPOOLSIZE,
				Constants.DEFAULT_INVOKER_MAXPOOLSIZE);
		ringBuffer = new RingBuffer<ResponseEvent>(ResponseEvent.EVENT_FACTORY, new SingleThreadedClaimStrategy(
				maxPoolSize), new BlockingWaitStrategy());
		sequenceBarrier = ringBuffer.newBarrier();
		handler = new ResponseEventHandler();
		batchEventProcessor = new BatchEventProcessor<ResponseEvent>(ringBuffer, sequenceBarrier, handler);
		ringBuffer.setGatingSequences(batchEventProcessor.getSequence());

		Thread responseProcessor = new Thread(batchEventProcessor);
		responseProcessor.setDaemon(true);
		responseProcessor.start();
	}

	public void stop() {
		batchEventProcessor.halt();
	}

	public void processResponse(final InvocationResponse response, final Client client) {
		long sequence = ringBuffer.next();
		ResponseEvent event = ringBuffer.get(sequence);
		event.setResponse(response);
		event.setClient(client);
		ringBuffer.publish(sequence);
	}

	// private static ThreadPool responseProcessThreadPool = new
	// DefaultThreadPool(
	// "Pigeon-Client-Response-Processor", 20, 300, new
	// LinkedBlockingQueue<Runnable>(50),
	// new CallerRunsPolicy());

	// private ClientManager clientManager = ClientManager.getInstance();

	// public void processResponse(final InvocationResponse response, final
	// Client client) {
	// Runnable task = new Runnable() {
	// public void run() {
	// if (response.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
	// clientManager.getHeartTask().processResponse(response, client);
	// } else {
	// ServiceInvocationRepository.getInstance().receiveResponse(response);
	// }
	// }
	// };
	// try {
	// // [v1.7.0, danson.liu]对于callback调用, 防止callback阻塞response
	// // handler thread pool线程池, 影响其他正常响应无法处理
	// responseProcessThreadPool.execute(task);
	// } catch (Exception ex) {
	// String msg = "Response execute fail:seq--" + response.getSequence() +
	// "\r\n";
	// logger.error(msg + ex.getMessage(), ex);
	// }
	// }

}
