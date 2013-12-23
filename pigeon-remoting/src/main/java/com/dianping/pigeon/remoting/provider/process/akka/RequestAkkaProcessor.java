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
import akka.routing.SmallestMailboxRouter;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

public class RequestAkkaProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestAkkaProcessor.class);
	final ActorSystem system = ActorSystem.create("Pigeon");
	ActorRef router;

	public RequestAkkaProcessor(ServerConfig serverConfig) {
		router = system.actorOf(new Props(RequestEventAkkaHandler.class).withRouter(new SmallestMailboxRouter(serverConfig.getMaxPoolSize())));
	}

	public void doStop() {
		system.shutdown();
	}

	public Future<InvocationResponse> doProcessRequest(final InvocationRequest request, final ProviderContext providerContext) {
		RequestEvent event = new RequestEvent();
		event.setProviderContext(providerContext);
		router.tell(event, null);
		return null;
	}

}
