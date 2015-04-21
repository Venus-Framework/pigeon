/**
 * Dianping.com Inc.
 * Copyright (c) 00-0 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.process.actor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import scala.concurrent.duration.Duration;
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
import com.dianping.pigeon.remoting.provider.process.AbstractRequestProcessor;
import com.dianping.pigeon.remoting.provider.process.event.RequestEvent;

/**
 * 
 * @author xiangwu
 * 
 */
public class RequestActorProcessor extends AbstractRequestProcessor {

	private static final Logger logger = LoggerLoader.getLogger(RequestActorProcessor.class);
	private final ActorSystem system = ActorSystem.create("Pigeon-Provider-Request-Processor");
	ActorRef defaultActor = null;
	private int lowerBound = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.lowerbound", 5);
	private int upperBound = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.upperbound", 300);
	private int pressureThreshold = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.pressurethreshold", 1);
	private double rampupRate = ConfigManagerLoader.getConfigManager().getDoubleValue(
			"pigeon.provider.actor.mailbox.rampuprate", 0.2d);
	private double backoffThreshold = ConfigManagerLoader.getConfigManager().getDoubleValue(
			"pigeon.provider.actor.mailbox.backoffthreshold", 0.3d);
	private double backoffRate = ConfigManagerLoader.getConfigManager().getDoubleValue(
			"pigeon.provider.actor.mailbox.backoffrate", 0.1d);
	private int messagesPerResize = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.messagesperresize", 10);
	private int stopDelay = ConfigManagerLoader.getConfigManager().getIntValue(
			"pigeon.provider.actor.mailbox.stopdelay", 1);

	private boolean useSharedActor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.provider.actor.shared", true);

	private ConcurrentHashMap<String, ActorInfo> serviceActors = null;

	public RequestActorProcessor(ServerConfig serverConfig) {
		serviceActors = new ConcurrentHashMap<String, ActorInfo>();
		Resizer resizer = new DefaultResizer(lowerBound, upperBound, pressureThreshold, rampupRate, backoffThreshold,
				backoffRate, Duration.create(stopDelay, TimeUnit.SECONDS), messagesPerResize);
		Props actorProps = Props.create(RequestEventActor.class, requestContextMap);
		SmallestMailboxRouter router = new SmallestMailboxRouter(resizer);
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
		StringBuilder str = new StringBuilder();
		for (String key : serviceActors.keySet()) {
			ActorInfo actorInfo = serviceActors.get(key);
			str.append("[").append(key).append("=").append(getActorStatistics(actorInfo)).append("],");
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
			Resizer resizer = new DefaultResizer(lowerBound, upperBound, pressureThreshold, rampupRate,
					backoffThreshold, backoffRate, Duration.create(stopDelay, TimeUnit.SECONDS), messagesPerResize);
			Props actorProps = Props.create(RequestEventActor.class, requestContextMap);
			SmallestMailboxRouter router = new SmallestMailboxRouter(resizer);
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