/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.DefaultResizer;
import akka.routing.Resizer;
import akka.routing.SmallestMailboxRouter;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.process.AbstractResponseProcessor;
import com.dianping.pigeon.remoting.invoker.process.event.ResponseEvent;

public class ResponseActorProcessor extends AbstractResponseProcessor {

	final ActorSystem system = ActorSystem.create("Pigeon-Invoker-Response-Processor");
	ActorRef router;
	int mailboxMinSize = ConfigManagerLoader.getConfigManager().getIntValue("pigeon.invoker.actor.mailbox.minsize", 10);
	int mailboxMaxSize = ConfigManagerLoader.getConfigManager()
			.getIntValue("pigeon.invoker.actor.mailbox.maxsize", 100);

	public ResponseActorProcessor() {
		if (mailboxMinSize <= 0) {
			mailboxMinSize = 10;
		}
		if (mailboxMaxSize <= 0) {
			mailboxMaxSize = 100;
		}
		if (mailboxMaxSize < mailboxMinSize) {
			mailboxMaxSize = mailboxMinSize;
		}
		Resizer resizer = new DefaultResizer(mailboxMinSize, mailboxMaxSize);
		Props actor = Props.create(ResponseEventActor.class);
		router = system.actorOf(actor.withRouter(new SmallestMailboxRouter(resizer)));
	}

	public void stop() {
		system.shutdown();
	}

	public void doProcessResponse(final InvocationResponse response, final Client client) {
		ResponseEvent event = new ResponseEvent();
		event.setResponse(response);
		event.setClient(client);
		router.tell(event, null);
	}

	@Override
	public String getProcessorStatistics() {
		return "";
	}
}
