/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestProcessor.class);
	private static ThreadPool requestProcessThreadPool = null;

	// private Disruptor<RequestEvent> disruptor;
	// private RingBuffer<RequestEvent> ringBuffer;
	// private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

	public RequestProcessor(ServerConfig serverConfig) {
		requestProcessThreadPool = new DefaultThreadPool("Pigeon-Server-Request-Processor",
				serverConfig.getCorePoolSize(), serverConfig.getMaxPoolSize(), new LinkedBlockingQueue<Runnable>(
						serverConfig.getWorkQueueSize()));
		// disruptor = new Disruptor<RequestEvent>(RequestEvent.EVENT_FACTORY,
		// serverConfig.getMaxPoolSize(), EXECUTOR);
		// RequestEventHandler handler = new
		// RequestEventHandler("event handler");
		// RequestEventHandler[] eventHandlers = new RequestEventHandler[] {
		// handler };
		// disruptor.handleEventsWith(eventHandlers);
		// ringBuffer = disruptor.start();

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
		// batchEventProcessor.halt();
		// disruptor.shutdown();
	}

	// public void processRequest(final InvocationRequest request, final
	// ProviderContext providerContext) {
	// long sequence = ringBuffer.next();
	// ringBuffer.get(sequence).setProviderContext(providerContext);
	// ringBuffer.publish(sequence);
	// }

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext) {
		Runnable requestExecutor = new Runnable() {
			@Override
			public void run() {
				try {
					ServiceInvocationHandler invocationHandler = RequestProcessHandlerFactory
							.selectInvocationHandler(providerContext.getRequest().getMessageType());
					if (invocationHandler != null) {
						invocationHandler.handle(providerContext);
					}
				} catch (Throwable t) {
					logger.error("Process request failed with invocation handler, you should never be here.", t);
				}
			}
		};
		requestProcessThreadPool.submit(requestExecutor);
	}

}
