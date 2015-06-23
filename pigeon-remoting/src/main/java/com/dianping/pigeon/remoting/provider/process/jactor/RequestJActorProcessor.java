/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.jactor;

import java.util.concurrent.Future;

import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.factory.ActorFactory;
import org.agilewiki.jactor.factory.JAFactory;
import org.agilewiki.jactor.lpc.JLPCActor;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

/**
 * 
 * @author xiangwu
 * 
 */
public class RequestJActorProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestJActorProcessor.class);
	private int threads = ConfigManagerLoader.getConfigManager().getIntValue("pigeon.provider.jactor.threads", 10);
	private MailboxFactory mailboxFactory = null;
	private RequestEventActor requestEventActor;

	public RequestJActorProcessor(ServerConfig serverConfig) {
		try {
			mailboxFactory = JAMailboxFactory.newMailboxFactory(threads);
			JAFactory jaFactory = new JAFactory();
			jaFactory.initialize(mailboxFactory.createAsyncMailbox());
			jaFactory.registerActorFactory(new ActorFactory("RequestEventActor") {

				@Override
				protected JLPCActor instantiateActor() throws Exception {
					RequestEventActor actor = new RequestEventActor(requestContextMap);
					return actor;
				}

			});
			requestEventActor = (RequestEventActor) JAFactory.newActor(jaFactory, "RequestEventActor");
		} catch (Exception e) {
			throw new RuntimeException("error while initializing jactor", e);
		}
	}

	@Override
	public String getProcessorStatistics() {
		return "jactor";
	}

	@Override
	public String getProcessorStatistics(InvocationRequest request) {
		return "jactor";
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) {
	}

	@Override
	public <T> void removeService(ProviderConfig<T> providerConfig) {
	}

	@Override
	public boolean needCancelRequest(InvocationRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Future<InvocationResponse> doProcessRequest(InvocationRequest request, ProviderContext providerContext) {
		requestContextMap.put(request, providerContext);
		RequestEvent event = new RequestEvent();
		event.setProviderContext(providerContext);
		try {
			requestEventActor.onReceive(event);
		} catch (Exception e) {
			throw new InvocationFailureException("error with jactor", e);
		}
		return null;
	}

	@Override
	public void doStart() {

	}

	@Override
	public void doStop() {
		if (mailboxFactory != null) {
			mailboxFactory.close();
		}
	}

}