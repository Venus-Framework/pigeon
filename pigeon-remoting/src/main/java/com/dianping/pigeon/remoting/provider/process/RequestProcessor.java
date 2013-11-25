/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.dianping.pigeon.threadpool.ThreadPool;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;

public class RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestProcessor.class);
	// private static ThreadPool requestProcessThreadPool = null;
	private RingBuffer<RequestEvent> ringBuffer;
	private SequenceBarrier sequenceBarrier;
	private RequestEventHandler handler;
	private BatchEventProcessor<RequestEvent> batchEventProcessor;

	public RequestProcessor(ServerConfig serverConfig) {
		// requestProcessThreadPool = new
		// DefaultThreadPool("Pigeon-Server-Request-Processor",
		// serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new
		// LinkedBlockingQueue<Runnable>(
		// serverConfig.getWorkQueueSize()));

		ringBuffer = new RingBuffer<RequestEvent>(RequestEvent.EVENT_FACTORY, new SingleThreadedClaimStrategy(
				serverConfig.getMaxPoolSize()), new BlockingWaitStrategy());
		sequenceBarrier = ringBuffer.newBarrier();
		handler = new RequestEventHandler();
		batchEventProcessor = new BatchEventProcessor<RequestEvent>(ringBuffer, sequenceBarrier, handler);
		ringBuffer.setGatingSequences(batchEventProcessor.getSequence());

		Thread requestProcessor = new Thread(batchEventProcessor);
		requestProcessor.setDaemon(true);
		requestProcessor.start();
	}
	
	public void stop() {
		batchEventProcessor.halt();
	}

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext) {
		long sequence = ringBuffer.next();
		ringBuffer.get(sequence).setProviderContext(providerContext);
		ringBuffer.publish(sequence);
	}

	// public void processRequest(final InvocationRequest request, final
	// ProviderContext providerContext) {
	// Runnable requestExecutor = new Runnable() {
	// @Override
	// public void run() {
	// try {
	// ServiceInvocationHandler invocationHandler = RequestProcessHandlerFactory
	// .selectInvocationHandler(providerContext.getRequest().getMessageType());
	// if (invocationHandler != null) {
	// invocationHandler.handle(providerContext);
	// }
	// } catch (Throwable t) {
	// logger.error("Process request failed with invocation handler, you should never be here.",
	// t);
	// }
	// }
	// };
	// requestProcessThreadPool.submit(requestExecutor);
	// }

}
