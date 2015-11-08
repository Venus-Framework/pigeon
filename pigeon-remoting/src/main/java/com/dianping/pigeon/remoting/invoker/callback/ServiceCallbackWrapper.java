/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.callback;

import java.io.Serializable;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.monitor.MonitorTransaction;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.util.ContextUtils;

public class ServiceCallbackWrapper implements Callback {

	private static final Logger logger = LoggerLoader.getLogger(ServiceCallbackWrapper.class);

	private static final Monitor monitor = MonitorLoader.getMonitor();

	private InvocationResponse response;

	private InvocationRequest request;

	private Client client;

	private ServiceCallback callback;

	private InvokerContext invocationContext;

	public ServiceCallbackWrapper(InvokerContext invocationContext, ServiceCallback callback) {
		this.invocationContext = invocationContext;
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
				long currentTime = System.currentTimeMillis();
				this.callback.callback(response.getReturn());
				if (request.getTimeout() > 0 && request.getCreateMillisTime() > 0
						&& request.getCreateMillisTime() + request.getTimeout() < currentTime) {
					StringBuilder msg = new StringBuilder();
					msg.append("request callback timeout:").append(request);
					Exception te = new RequestTimeoutException(msg.toString());
					te.setStackTrace(new StackTraceElement[] {});
					logger.error(msg);
					if (monitor != null) {
						monitor.logError(te);
					}
				}
//				MonitorTransaction transaction = invocationContext.getMonitorTransaction();
//				if (transaction != null) {
//					MonitorTransaction newTransaction = monitor.copyTransaction("PigeonCall", transaction.getUri(),
//							invocationContext, transaction);
//					if (newTransaction != null) {
//						if (response != null) {
//							newTransaction.logEvent("PigeonCall.responseSize",
//									SizeMonitor.getInstance().getLogSize(response.getSize()), "" + response.getSize());
//						}
//						newTransaction.setStatusOk();
//						newTransaction.complete();
//						newTransaction.setDurationStart(transaction.getDurationStart());
//						newTransaction.setDuration(System.currentTimeMillis() - transaction.getDurationStart());
//					}
//					transaction.complete();
//				}
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				RpcException cause = InvokerUtils.toRpcException(response);
				StringBuilder sb = new StringBuilder();
				sb.append("callback service exception\r\n").append("seq:").append(request.getSequence())
						.append(",callType:").append(request.getCallType()).append("\r\nservice:")
						.append(request.getServiceName()).append(",method:").append(request.getMethodName())
						.append("\r\nhost:").append(client.getHost()).append(":").append(client.getPort());
				logger.error(sb.toString(), cause);
				monitor.logError(sb.toString(), cause);
				this.callback.frameworkException(new DPSFException(cause));
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				Throwable cause = InvokerUtils.toApplicationException(response);
				Exception businessException = (Exception) cause;
				if (Constants.LOG_INVOKER_APP_EXCEPTION) {
					logger.error("error with remote business callback", businessException);
					monitor.logError("error with remote business callback", businessException);
				}
				this.callback.serviceException(businessException);
			} else {
				RpcException e = new InvalidParameterException("unsupported response with message type:"
						+ response.getMessageType());
				monitor.logError(e);
			}
		} catch (Throwable e) {
			logger.error("error while executing service callback", e);
		}
		setResponseContext(response);
		// TIMELINE_remove
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
