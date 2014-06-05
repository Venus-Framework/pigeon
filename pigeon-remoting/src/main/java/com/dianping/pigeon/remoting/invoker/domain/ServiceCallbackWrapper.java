/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import org.apache.log4j.Logger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLogger;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class ServiceCallbackWrapper implements Callback {

	private static final Logger logger = LoggerLoader.getLogger(ServiceCallbackWrapper.class);

	private static final MonitorLogger monitorLogger = ExtensionLoader.getExtension(Monitor.class).getLogger();

	private InvocationResponse response;

	private InvocationRequest request;

	private Client client;

	private ServiceCallback callback;

	public ServiceCallbackWrapper(ServiceCallback callback) {
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				if (logger.isDebugEnabled()) {
					logger.debug("response:" + response);
					logger.debug("callback:" + callback);
				}
				this.callback.callback(response.getReturn());
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				RpcException cause = InvokerUtils.toRpcException(response);
				StringBuilder sb = new StringBuilder();
				sb.append("callback service exception\r\n").append("seq:").append(request.getSequence())
						.append(",callType:").append(request.getCallType()).append("\r\nservice:")
						.append(request.getServiceName()).append(",method:").append(request.getMethodName())
						.append("\r\nhost:").append(client.getHost()).append(":").append(client.getPort());
				logger.error(sb.toString(), cause);
				monitorLogger.logError(sb.toString(), cause);
				this.callback.frameworkException(new DPSFException(cause));
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				Throwable cause = InvokerUtils.toApplicationException(response);
				Exception businessException = (Exception) cause;
				logger.error("error with remote business callback", businessException);
				monitorLogger.logError("error with remote business callback", businessException);
				this.callback.serviceException(businessException);
			}
		} catch (Exception e) {
			logger.error("error while executing service callback", e);
		}
		// TIMELINE_remove
	}

	@Override
	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public Client getClient() {
		return this.client;
	}

	@Override
	public void callback(InvocationResponse response) {
		this.response = response;
	}

	@Override
	public void setRequest(InvocationRequest request) {
		this.request = request;
	}

}
