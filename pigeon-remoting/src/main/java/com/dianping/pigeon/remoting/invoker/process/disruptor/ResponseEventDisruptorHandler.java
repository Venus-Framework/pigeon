/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process;

import org.apache.log4j.Logger;

import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.process.event.ResponseEvent;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.lmax.disruptor.EventHandler;

public class ResponseEventHandler implements EventHandler<ResponseEvent> {

	private static final Logger logger = LoggerLoader.getLogger(ResponseEventHandler.class);
	private ClientManager clientManager = ClientManager.getInstance();

	public void onEvent(ResponseEvent event, long sequence, boolean endOfBatch) throws Exception {
		if (event.getResponse().getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			clientManager.getHeartTask().processResponse(event.getResponse(), event.getClient());
		} else {
			ServiceInvocationRepository.getInstance().receiveResponse(event.getResponse());
		}
	}
}
