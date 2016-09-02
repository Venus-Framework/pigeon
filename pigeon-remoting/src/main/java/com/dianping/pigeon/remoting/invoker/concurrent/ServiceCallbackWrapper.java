/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.concurrent;

import java.io.Serializable;
import java.util.Map;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.BadResponseException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;
import com.dianping.pigeon.util.ContextUtils;

public class ServiceCallbackWrapper implements Callback {

	private static final Logger logger = LoggerLoader.getLogger(ServiceCallbackWrapper.class);

	private static final Monitor monitor = MonitorLoader.getMonitor();

	private InvocationResponse response;

	private InvocationRequest request;

	private Client client;

	private InvocationCallback callback;

	private InvokerContext invocationContext;

	public ServiceCallbackWrapper(InvokerContext invocationContext, InvocationCallback callback) {
		this.invocationContext = invocationContext;
		this.callback = callback;
	}

	@Override
	public void run() {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		MonitorTransaction transaction = null;
		long currentTime = System.currentTimeMillis();
		String addr = null;
		if (client != null) {
			addr = client.getAddress();
		}
		try {
			setResponseContext(response);
			if (Constants.MONITOR_ENABLE) {
				String callInterface = InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
						invocationContext.getMethodName(), invocationContext.getParameterTypes());
				transaction = monitor.createTransaction("PigeonCallback", callInterface, invocationContext);
			}
			if (transaction != null) {
				transaction.setStatusOk();
				transaction.addData("CallType", invokerConfig.getCallType());
				transaction.addData("Timeout", invokerConfig.getTimeout());
				transaction.addData("Serialize", request.getSerialize());
				if (response != null && response.getSize() > 0) {
					String respSize = SizeMonitor.getInstance().getLogSize(response.getSize());
					if (respSize != null) {
						monitor.logEvent("PigeonCall.responseSize", respSize, "" + response.getSize());
					}
					invocationContext.getTimeline().add(new TimePoint(TimePhase.R, response.getCreateMillisTime()));
					invocationContext.getTimeline().add(new TimePoint(TimePhase.R, currentTime));
				}
			}
			if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
					&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
				StringBuilder msg = new StringBuilder();
				msg.append("request callback timeout:").append(request);
				Exception e = new RequestTimeoutException(msg.toString());
				e.setStackTrace(new StackTraceElement[] {});
				DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
				ExceptionManager.INSTANCE.logRpcException(addr, invocationContext.getInvokerConfig().getUrl(),
						invocationContext.getMethodName(), "request callback timeout", e, request, response,
						transaction);
			}
		} finally {
			try {
				if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
					completeTransaction(transaction);

					this.callback.onSuccess(response.getReturn());
				} else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
					RpcException e = ExceptionManager.INSTANCE.logRemoteCallException(addr,
							invocationContext.getInvokerConfig().getUrl(), invocationContext.getMethodName(),
							"remote call error with callback", request, response, transaction);
					DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
					completeTransaction(transaction);

					this.callback.onFailure(e);
				} else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
					Exception e = ExceptionManager.INSTANCE
							.logRemoteServiceException("remote service biz error with callback", request, response);
					completeTransaction(transaction);

					this.callback.onFailure(e);
				} else {
					RpcException e = new BadResponseException(response.toString());
					monitor.logError(e);

					completeTransaction(transaction);
				}
			} catch (Throwable e) {
				logger.error("error while executing service callback", e);
			}
		}
	}

	private void completeTransaction(MonitorTransaction transaction) {
		if (transaction != null) {
			invocationContext.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
			try {
				transaction.complete();
			} catch (Throwable e) {
				monitor.logMonitorError(e);
			}
		}
	}

	protected void setResponseContext(InvocationResponse response) {
		if (response != null) {
			Map<String, Serializable> responseValues = response.getResponseValues();
			if (responseValues != null) {
				ContextUtils.setResponseContext(responseValues);
			}
		}
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

	@Override
	public void dispose() {

	}

}
