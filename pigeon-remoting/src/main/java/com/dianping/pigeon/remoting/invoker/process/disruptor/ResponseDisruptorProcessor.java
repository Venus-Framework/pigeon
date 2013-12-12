/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.disruptor;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.event.ResponseEvent;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;

public class ResponseDisruptorProcessor implements ResponseProcessor {

	private static final Logger logger = LoggerLoader.getLogger(ResponseDisruptorProcessor.class);
	private RingBuffer<ResponseEvent> ringBuffer;
	private SequenceBarrier sequenceBarrier;
	private ResponseEventDisruptorHandler handler;
	private BatchEventProcessor<ResponseEvent> batchEventProcessor;
	private ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	public ResponseDisruptorProcessor() {
		int maxPoolSize = configManager.getIntValue(Constants.KEY_INVOKER_MAXPOOLSIZE,
				Constants.DEFAULT_INVOKER_MAXPOOLSIZE);
		EventFactory<ResponseEvent> EVENT_FACTORY = new EventFactory<ResponseEvent>() {
			public ResponseEvent newInstance() {
				return new ResponseEvent();
			}
		};
		ringBuffer = new RingBuffer<ResponseEvent>(EVENT_FACTORY, new SingleThreadedClaimStrategy(
				maxPoolSize), new BlockingWaitStrategy());
		sequenceBarrier = ringBuffer.newBarrier();
		handler = new ResponseEventDisruptorHandler();
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

}
