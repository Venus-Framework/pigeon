/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.akka;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.DefaultResizer;
import akka.routing.Resizer;
import akka.routing.RouterConfig;
import akka.routing.SmallestMailboxRouter;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

/**
 * 
 * @author xiangwu
 * 
 */
public class RequestAkkaProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestAkkaProcessor.class);
	private final ActorSystem system = ActorSystem.create("Pigeon-Provider-Request-Processor");
	ActorRef defaultActor = null;
	private int lowerBound = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.akka.mailbox.lowerbound", 5);
	private int upperBound = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.akka.mailbox.upperbound", 300);

	private boolean useSharedActor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.akka.shared", true);

	private ConcurrentHashMap<String, ActorInfo> serviceActors = null;

	public RequestAkkaProcessor(ServerConfig serverConfig) {
		serviceActors = new ConcurrentHashMap<String, ActorInfo>();
		Props actorProps = Props.create(RequestEventActor.class, requestContextMap);
		Resizer resizer = new DefaultResizer(lowerBound, upperBound);
		RouterConfig router = new SmallestMailboxRouter(resizer);
		defaultActor = system.actorOf(actorProps.withRouter(router));
	}

	private static class ActorInfo {
		private ActorRef actor;
		private SmallestMailboxRouter router;

		public ActorRef getActor() {
			return actor;
		}

		public void setActor(ActorRef actor) {
			this.actor = actor;
		}

		public SmallestMailboxRouter getRouter() {
			return router;
		}

		public void setRouter(SmallestMailboxRouter router) {
			this.router = router;
		}

	}

	public void stop() {
		system.shutdown();
	}

	@Override
	public String getProcessorStatistics() {
		return "akka";
	}

	@Override
	public String getProcessorStatistics(InvocationRequest request) {
		return "akka";
	}

	private String getActorStatistics(ActorInfo actorInfo) {
		return actorInfo.getRouter().toString();
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) {
		if (!useSharedActor) {
			int minSize = lowerBound;
			int maxSize = providerConfig.getActives();
			if (minSize <= 0) {
				minSize = 10;
			}
			if (maxSize <= 0) {
				maxSize = upperBound;
			}
			if (maxSize < minSize) {
				maxSize = minSize;
			}

			Props actorProps = Props.create(RequestEventActor.class, requestContextMap);
			SmallestMailboxRouter router = null;
			if (lowerBound == upperBound) {
				router = new SmallestMailboxRouter(lowerBound);
			} else {
				Resizer resizer = new DefaultResizer(lowerBound, upperBound);
				router = new SmallestMailboxRouter(resizer);
			}
			ActorRef actor = system.actorOf(actorProps.withRouter(router));
			ActorInfo actorInfo = new ActorInfo();
			actorInfo.setActor(actor);
			actorInfo.setRouter(router);
			serviceActors.putIfAbsent(providerConfig.getUrl(), actorInfo);
		}
	}

	@Override
	public <T> void removeService(ProviderConfig<T> providerConfig) {
		if (!useSharedActor) {
			serviceActors.remove(providerConfig.getUrl());
		}
	}

	private ActorInfo getActor(InvocationRequest request) {
		ActorInfo actor = serviceActors.get(request.getServiceName());
		return actor;
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
		if (useSharedActor) {
			defaultActor.tell(event, null);
		} else {
			ActorInfo actorInfo = getActor(request);
			if (actorInfo != null) {
				actorInfo.getActor().tell(event, null);
			} else {
				defaultActor.tell(event, null);
			}
		}
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