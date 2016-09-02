/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;

public class ServiceFutureImpl extends CallbackFuture implements Future {

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
	public Object get() throws InterruptedException, ExecutionException {
		return get(this.timeout);
	}

	public Object get(long timeoutMillis) throws InterruptedException, ExecutionException {
		InvocationResponse response = null;
		long start = System.currentTimeMillis();
		String addr = null;
		if (client != null) {
			addr = client.getAddress();
		}
		if (transaction != null) {
			transaction.addData("FutureTimeout", timeoutMillis);
			invocationContext.getTimeline().add(new TimePoint(TimePhase.F, start));
		}
		try {
			try {
				response = super.waitResponse(timeoutMillis);
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
				ExceptionManager.INSTANCE.logRpcException(addr, invocationContext.getInvokerConfig().getUrl(),
						invocationContext.getMethodName(), "error with future call", e, request, response, transaction);
				throw e;
			}

			setResponseContext(response);

			if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				return response.getReturn();
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				RpcException e = ExceptionManager.INSTANCE.logRemoteCallException(addr,
						invocationContext.getInvokerConfig().getUrl(), invocationContext.getMethodName(),
						"remote call error with future call", request, response, transaction);
				if (e != null) {
					throw e;
				}
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				Throwable e = ExceptionManager.INSTANCE
						.logRemoteServiceException("remote service biz error with future call", request, response);
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
				} catch (RuntimeException e) {
					monitor.logMonitorError(e);
				}
			}
		}
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws java.lang.InterruptedException,
			java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
		long timeoutMs = unit.toMillis(timeout);
		try {
			return get(timeoutMs);
		} catch (RequestTimeoutException e) {
			throw new TimeoutException(timeoutMs + "ms timeout:" + e.getMessage());
		} catch (InterruptedException e) {
			throw e;
		}
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
			} catch (RuntimeException e) {
			}
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return cancel();
	}
}
