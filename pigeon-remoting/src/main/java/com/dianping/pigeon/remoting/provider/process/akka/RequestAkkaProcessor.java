/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.akka;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.SmallestMailboxRouter;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

public class RequestAkkaProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestAkkaProcessor.class);
	final ActorSystem system = ActorSystem.create("Pigeon");
	ActorRef router;

	public RequestAkkaProcessor(ServerConfig serverConfig) {
		UntypedActorFactory factory = new UntypedActorFactory() {
			public UntypedActor create() {
				return new RequestEventAkkaHandler(requestContextMap);
			}
		};
		router = system
				.actorOf(new Props(factory).withRouter(new SmallestMailboxRouter(serverConfig.getMaxPoolSize())));
	}

	public void stop() {
		system.shutdown();
	}

	@Override
	public String getProcessorStatistics() {
		return "";
	}

	@Override
	public String getProcessorStatistics(InvocationRequest request) {
		return "";
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void removeService(ProviderConfig<T> providerConfig) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean needCancelRequest(InvocationRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Future<InvocationResponse> doProcessRequest(InvocationRequest request, ProviderContext providerContext) {
		RequestEvent event = new RequestEvent();
		event.setProviderContext(providerContext);
		requestContextMap.put(request, providerContext);
		router.tell(event, null);
		return null;
	}

	@Override
	public void doStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doStop() {
		// TODO Auto-generated method stub

	}

}