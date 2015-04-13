/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.actor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.DefaultResizer;
import akka.routing.Resizer;
import akka.routing.SmallestMailboxRouter;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

/**
 * 
 * @author xiangwu
 * 
 */
public class RequestActorProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestActorProcessor.class);
	private final ActorSystem system = ActorSystem.create("Pigeon-Provider-Request-Processor");
	private int mailboxMinSize = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.minsize", 5);
	private int mailboxMaxSize = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.maxsize", 300);
	private ConcurrentHashMap<String, ActorInfo> serviceActors = null;

	public RequestActorProcessor(ServerConfig serverConfig) {
		serviceActors = new ConcurrentHashMap<String, ActorInfo>();
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
		StringBuilder str = new StringBuilder();
		for (String key : serviceActors.keySet()) {
			ActorInfo actorInfo = serviceActors.get(key);
			str.append("service:").append(key).append(",actor info:[").append(getActorStatistics(actorInfo))
					.append("]");
		}
		return str.toString();
	}

	@Override
	public String getProcessorStatistics(InvocationRequest request) {
		ActorInfo actorInfo = getActor(request);
		if (actorInfo != null) {
			return getActorStatistics(actorInfo);
		}
		return "";
	}

	private String getActorStatistics(ActorInfo actorInfo) {
		DefaultResizer resizer = (DefaultResizer) actorInfo.getRouter().resizer().get();
		StringBuilder str = new StringBuilder();
		str.append("resizer:upperBound:").append(resizer.upperBound());
		str.append(",lowerBound:").append(resizer.lowerBound());
		return str.toString();
	}

	@Override
	public <T> void addService(ProviderConfig<T> providerConfig) {
		int minSize = mailboxMinSize;
		int maxSize = providerConfig.getActives();
		if (minSize <= 0) {
			minSize = 10;
		}
		if (maxSize <= 0) {
			maxSize = mailboxMaxSize;
		}
		if (maxSize < minSize) {
			maxSize = minSize;
		}
		Resizer resizer = new DefaultResizer(minSize, maxSize);
		Props actorProps = Props.create(RequestEventActor.class, requestContextMap);
		SmallestMailboxRouter router = new SmallestMailboxRouter(resizer);
		ActorRef actor = system.actorOf(actorProps.withRouter(router));
		ActorInfo actorInfo = new ActorInfo();
		actorInfo.setActor(actor);
		actorInfo.setRouter(router);
		serviceActors.putIfAbsent(providerConfig.getUrl(), actorInfo);
	}

	@Override
	public <T> void removeService(ProviderConfig<T> providerConfig) {
		serviceActors.remove(providerConfig.getUrl());
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
		ActorInfo actorInfo = getActor(request);
		if (actorInfo != null) {
			actorInfo.getActor().tell(event, null);
		} else {
			requestContextMap.remove(request);
			throw new InvocationFailureException(ProviderUtils.getRequestDetailInfo("no actor found", providerContext,
					request));
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