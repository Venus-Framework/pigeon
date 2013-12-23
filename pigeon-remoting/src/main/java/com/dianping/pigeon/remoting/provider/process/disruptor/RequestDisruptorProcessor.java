/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class RequestDisruptorProcessor extends AbstractRequestProcessor {

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
		disruptor = new Disruptor<RequestEvent>(eventFactory, 256, EXECUTOR);
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

	public void doStop() {
		disruptor.shutdown();
	}

	public Future<InvocationResponse> doProcessRequest(final InvocationRequest request, final ProviderContext providerContext) {
		long sequence = ringBuffer.next();
		ringBuffer.get(sequence).setProviderContext(providerContext);
		ringBuffer.publish(sequence);
		return null;
	}

}
