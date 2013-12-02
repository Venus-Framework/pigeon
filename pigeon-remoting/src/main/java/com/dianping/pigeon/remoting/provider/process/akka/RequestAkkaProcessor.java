/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.akka;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxRouter;

import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.provider.component.context.ProviderContext;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

public class RequestAkkaProcessor implements RequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestAkkaProcessor.class);
	final ActorSystem system = ActorSystem.create("Pigeon");
	ActorRef router;

	public RequestAkkaProcessor(ServerConfig serverConfig) {
		router = system.actorOf(new Props(RequestEventAkkaHandler.class).withRouter(new SmallestMailboxRouter(serverConfig.getMaxPoolSize())));
	}

	public void stop() {
		system.shutdown();
	}

	public void processRequest(final InvocationRequest request, final ProviderContext providerContext) {
		RequestEvent event = new RequestEvent();
		event.setProviderContext(providerContext);
		router.tell(event, null);
	}

}
