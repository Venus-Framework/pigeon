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
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.InvalidParameterException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
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

	private void addMonitorInfo() {
		InvokerConfig<?> invokerConfig = invocationContext.getInvokerConfig();
		MonitorTransaction transaction = null;
		long currentTime = System.currentTimeMillis();
		try {

			if (Constants.INVOKER_CALLBACK_MONITOR_ENABLE) {
				String callInterface = InvocationUtils.getRemoteCallFullName(invokerConfig.getUrl(),
						invocationContext.getMethodName(), invocationContext.getParameterTypes());
				transaction = monitor.createTransaction("PigeonCallback", callInterface, invocationContext);
			}
			if (transaction != null) {
				transaction.setStatusOk();
				transaction.addData("CallType", invokerConfig.getCallType());
				transaction.addData("Timeout", invokerConfig.getTimeout());
				transaction.addData("Serialize", invokerConfig.getSerialize());
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
				Exception te = new RequestTimeoutException(msg.toString());
				te.setStackTrace(new StackTraceElement[] {});
				if (Constants.INVOKER_LOG_TIMEOUT_EXCEPTION) {
					logger.error(msg);
				}
				if (monitor != null) {
					monitor.logError(te);
				}
			}
		} catch (Throwable e) {
			monitor.logMonitorError(e);
		} finally {
			if (transaction != null) {
				try {
					transaction.complete();
				} catch (Throwable e) {
					monitor.logMonitorError(e);
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				addMonitorInfo();
				this.callback.callback(response.getReturn());
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
				if (Constants.INVOKER_LOG_APP_EXCEPTION) {
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
