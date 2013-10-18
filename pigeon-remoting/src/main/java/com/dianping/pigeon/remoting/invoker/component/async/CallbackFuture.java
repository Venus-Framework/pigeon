/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.component.async;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.exception.PigeonRuntimeException;
import com.dianping.pigeon.remoting.common.component.RequestError;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.exception.NetworkTimeoutException;
import com.dianping.pigeon.remoting.common.util.Constants;
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

	private static final Logger logger = Logger.getLogger(CallbackFuture.class);

	private DPSFResponse response;

	private CallFuture future;

	private boolean done = false;
	private boolean concelled = false;
	private boolean success = false;

	private RequestError error;

	private DPSFRequest request;

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

	public void callback(DPSFResponse response) {
		this.response = response;
	}

	public DPSFResponse get() throws InterruptedException, NetworkException {
		return get(Long.MAX_VALUE);
	}

	public DPSFResponse get(long timeoutMillis) throws InterruptedException, NetworkException {
		synchronized (this) {
			long start = request.getCreateMillisTime();
			while (!this.done) {
				long timeoutMillis_ = timeoutMillis - (System.currentTimeMillis() - start);
				if (timeoutMillis_ <= 0) {
					this.error = RequestError.TIMEOUT;
					StringBuffer sb = new StringBuffer();
					sb.append(this.error.getMsg()).append("\r\n seq:").append(request.getSequence())
							.append("\r\n callType:").append(request.getCallType()).append("\r\n serviceName:")
							.append(request.getServiceName()).append("\r\n methodName:")
							.append(request.getMethodName()).append("\r\n host:").append(client.getHost()).append(":")
							.append(client.getPort()).append("\r\n timeout:" + request.getTimeout())
							.append("\r\n Parameters:" + request.getParameters());

					RpcEventUtils.clientTimeOutEvent(request, client.getAddress());

					NetworkTimeoutException netTimeoutException = new NetworkTimeoutException(sb.toString());
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
				logger.error("response is" + response);
				if (response instanceof DefaultResponse) {
					cause = (Throwable) response.getReturn();
				} else {
					cause = new PigeonRuntimeException(response.getCause());
				}
				StringBuffer sb = new StringBuffer();
				sb.append(cause.getMessage()).append("\r\n");
				sb.append("Remote Service Exception Info *************\r\n")
						// .append(" token:").append(ContextUtil.getToken(this.response.getContext())).append("\r\n")
						.append(" seq:").append(request.getSequence()).append(" callType:")
						.append(request.getCallType()).append("\r\n serviceName:").append(request.getServiceName())
						.append(" methodName:").append(request.getMethodName()).append("\r\n host:")
						.append(client.getHost()).append(":").append(client.getPort())
						.append("\r\n timeout:" + request.getTimeout());
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

	public DPSFResponse get(long timeout, TimeUnit unit) throws InterruptedException, NetworkException {
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

	// public DPSFFuture getFuture(ChannelFuture future) {
	//
	// this.future = future;
	// return this;
	// }

	public void fail(RequestError error) {
		synchronized (this) {
			this.error = error;
			this.done = true;
			this.concelled = false;
			this.success = false;
			this.future = null;
			this.notifyAll();
		}

	}

	public void setRequest(DPSFRequest request) {
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
