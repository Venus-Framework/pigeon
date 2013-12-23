/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.NetTimeoutException;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.util.RpcEventUtils;
import com.dianping.pigeon.util.ContextUtils;

/**
 * 
 * @author jianhuihuang
 * @version $Id: CallbackFuture.java, v 0.1 2013-6-29 下午8:53:13 jianhuihuang Exp
 *          $
 */
public class CallbackFuture implements Callback, CallFuture {

	private static final Logger logger = LoggerLoader.getLogger(CallbackFuture.class);

	private InvocationResponse response;
	private CallFuture future;
	private boolean done = false;
	private boolean concelled = false;
	private boolean success = false;
	private InvocationRequest request;
	private Client client;

	public void run() {
		synchronized (this) {
			this.done = true;

			if (this.response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				this.success = true;
			}

			this.notifyAll();
		}
	}

	public void callback(InvocationResponse response) {
		this.response = response;
	}

	public InvocationResponse get() throws InterruptedException {
		return get(Long.MAX_VALUE);
	}

	public InvocationResponse get(long timeoutMillis) throws InterruptedException {
		synchronized (this) {
			long start = request.getCreateMillisTime();
			while (!this.done) {
				long timeoutMillis_ = timeoutMillis - (System.currentTimeMillis() - start);
				if (timeoutMillis_ <= 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("request timeout, current time:").append(System.currentTimeMillis())
							.append("\r\nrequest:").append(request).append("\r\nhost:").append(client.getHost())
							.append(":").append(client.getPort());

					RpcEventUtils.clientTimeOutEvent(request, client.getAddress());

					NetTimeoutException netTimeoutException = new NetTimeoutException(sb.toString());
					throw netTimeoutException;
				} else {
					this.wait(timeoutMillis_);
				}
			}
			Object context = ContextUtils.getContext();
			if (context != null) {
				Integer order = ContextUtils.getOrder(this.response.getContext());
				if (order != null && order > 0) {
					ContextUtils.setOrder(context, order);
				}
				if (this.success) {
					// 传递业务上下文
					ContextUtils.addSuccessContext(this.response.getContext());
				} else {
					// 传递业务上下文
					ContextUtils.addFailedContext(this.response.getContext());
				}
			}
			if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION
					|| response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				Throwable cause = null;
				if (response instanceof InvocationResponse) {
					cause = InvocationUtils.toInvocationThrowable(response.getReturn());
				}
				if (cause == null) {
					cause = new RuntimeException(response.getCause());
				}
				StringBuilder sb = new StringBuilder();
				sb.append("remote service exception\r\nrequest:").append(request).append("\r\nhost:")
						.append(client.getHost()).append(":").append(client.getPort()).append("\r\nresponse:")
						.append(response);
				Field field;
				try {
					field = Throwable.class.getDeclaredField("detailMessage");
					field.setAccessible(true);
					field.set(cause, sb.toString());
				} catch (Exception e) {
					logger.error("can not be happened.....", e);
				}
				logger.error(cause.getMessage(), cause);
			}
			return this.response;
		}
	}

	public InvocationResponse get(long timeout, TimeUnit unit) throws InterruptedException {
		return get(unit.toMillis(timeout));
	}

	public boolean cancel() {
		if (this.future != null) {
			synchronized (this) {
				this.concelled = this.future.cancel();
				this.notifyAll();
			}
		}
		return this.concelled;
	}

	public boolean isCancelled() {
		return this.concelled;
	}

	public boolean isDone() {
		return this.done;
	}

	public void setRequest(InvocationRequest request) {
		this.request = request;
	}

	@Override
	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public Client getClient() {
		return this.client;
	}

}
