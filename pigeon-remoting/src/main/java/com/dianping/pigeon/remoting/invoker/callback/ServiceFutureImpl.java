/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.callback;

import java.util.concurrent.TimeUnit;

import com.dianping.dpsf.async.ServiceFuture;
import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.ApplicationException;
import com.dianping.pigeon.remoting.common.exception.BadResponseException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;

public class ServiceFutureImpl extends CallbackFuture implements ServiceFuture {

	private static final Logger logger = LoggerLoader.getLogger(ServiceFutureImpl.class);

	private long timeout = Long.MAX_VALUE;

	protected Thread callerThread;

	protected InvokerContext invocationContext;

	public ServiceFutureImpl(InvokerContext invocationContext, long timeout) {
		super();
		this.timeout = timeout;
		this.invocationContext = invocationContext;
		callerThread = Thread.currentThread();
	}

	@Override
	public Object _get() throws InterruptedException {
		return _get(this.timeout);
	}

	@Override
	public Object _get(long timeoutMillis) throws InterruptedException {
		InvocationResponse response = null;
		long start = System.currentTimeMillis();
		if (transaction != null) {
			transaction.addData("FutureTimeout", timeoutMillis);
			invocationContext.getTimeline().add(new TimePoint(TimePhase.F, start));
		}
		try {
			try {
				response = super.getResponse(timeoutMillis);
				if (transaction != null && response != null) {
					String size = SizeMonitor.getInstance().getLogSize(response.getSize());
					if (size != null) {
						transaction.logEvent("PigeonCall.responseSize", size, "" + response.getSize());
					}
					invocationContext.getTimeline().add(new TimePoint(TimePhase.R, response.getCreateMillisTime()));
					invocationContext.getTimeline().add(new TimePoint(TimePhase.F, System.currentTimeMillis()));
				}
			} catch (RuntimeException e) {
				DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
				ExceptionManager.INSTANCE.logRpcException(client.getAddress(), request.getServiceName(),
						request.getMethodName(), "error with future call", e, transaction);
				throw e;
			}

			setResponseContext(response);

			if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				return response.getReturn();
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				StringBuilder msg = new StringBuilder();
				msg.append("remote call error with future call\r\nrequest:").append(request).append("\r\nhost:")
						.append(client.getHost()).append(":").append(client.getPort()).append("\r\nresponse:")
						.append(response);
				RpcException e = ExceptionManager.INSTANCE.logRemoteCallException(client.getAddress(),
						request.getServiceName(), request.getMethodName(), msg.toString(), response, transaction);
				if (e != null) {
					throw e;
				}
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				StringBuilder msg = new StringBuilder();
				msg.append("remote service biz error with future call\r\nrequest:").append(request).append("\r\nhost:")
						.append(client.getHost()).append(":").append(client.getPort()).append("\r\nresponse:")
						.append(response);
				Throwable e = ExceptionManager.INSTANCE.logRemoteServiceException(client.getAddress(),
						request.getServiceName(), request.getMethodName(), msg.toString(), response);
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else if (e != null) {
					throw new ApplicationException(e);
				}
			}
			RpcException e = new BadResponseException(response.toString());
			throw e;
		} finally {
			if (transaction != null) {
				invocationContext.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
				try {
					transaction.complete(start);
				} catch (Throwable e) {
					monitor.logMonitorError(e);
				}
			}
		}
	}

	@Override
	public Object _get(long timeout, TimeUnit unit) throws InterruptedException {
		return _get(unit.toMillis(timeout));
	}

	protected void processContext() {
		Thread currentThread = Thread.currentThread();
		if (currentThread == callerThread) {
			super.processContext();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (transaction != null) {
			try {
				transaction.complete();
			} catch (Throwable e) {
			}
		}
	}
}
