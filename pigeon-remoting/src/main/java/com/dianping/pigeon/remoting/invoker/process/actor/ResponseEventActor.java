/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.actor;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.invoker.process.event.ResponseEvent;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

public class ResponseEventActor extends UntypedActor {

	private static final Logger logger = LoggerLoader.getLogger(ResponseEventActor.class);

	public ResponseEventActor() {
	}

	@Override
	public void onReceive(Object message) throws Exception {
		ResponseEvent event = (ResponseEvent) message;
		try {
			ServiceInvocationRepository.getInstance().receiveResponse(event.getResponse());
		} catch (Throwable t) {
			logger.error("error while processing response", t);
		}
	}
}