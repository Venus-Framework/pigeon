/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class RequestDisruptorProcessor implements RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestDisruptorProcessor.class);
	private Disruptor<RequestEvent> disruptor;
	private RingBuffer<RequestEvent> ringBuffer;
	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

	public RequestDisruptorProcessor(ServerConfig serverConfig) {
		EventFactory<RequestEvent> eventFactory = new EventFactory<RequestEvent>() {
			public RequestEvent newInstance() {
				return new RequestEvent();
			}
		};
		disruptor = new Disruptor<RequestEvent>(eventFactory, serverConfig.getMaxPoolSize(), EXECUTOR);
		RequestEventDisruptorHandler handler = new RequestEventDisruptorHandler();
		RequestEventDisruptorHandler[] eventHandlers = new RequestEventDisruptorHandler[] { handler };
		disruptor.handleEventsWith(eventHandlers);
		ringBuffer = disruptor.start();

		// SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
		// BatchEventProcessor<RequestEvent> batchEventProcessor = new
		// BatchEventProcessor<RequestEvent>(ringBuffer, sequenceBarrier,
		// handler);
		// ringBuffer.setGatingSequences(batchEventProcessor.getSequence());
		// Thread requestProcessor = new Thread(batchEventProcessor);
		// requestProcessor.setDaemon(true);
		// requestProcessor.start();
	}

	public void stop() {
		disruptor.shutdown();
	}

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext) {
		long sequence = ringBuffer.next();
		ringBuffer.get(sequence).setProviderContext(providerContext);
		ringBuffer.publish(sequence);
	}

}
