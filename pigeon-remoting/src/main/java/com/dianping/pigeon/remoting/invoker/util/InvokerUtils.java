package com.dianping.pigeon.remoting.invoker.util;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.dpsf.exception.DPSFException;
import com.dianping.dpsf.exception.NetException;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.TimelineManager;
import com.dianping.pigeon.remoting.common.util.TimelineManager.Phase;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.Callback;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

public class InvokerUtils {

	private static ServiceInvocationRepository invocationRepository = ServiceInvocationRepository.getInstance();

	private static AtomicLong requestSequenceMaker = new AtomicLong();

	public static InvocationResponse sendRequest(Client client, InvocationRequest request, Callback callback) {
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			RemoteInvocationBean invocationBean = new RemoteInvocationBean();
			invocationBean.request = request;
			invocationBean.callback = callback;
			callback.setRequest(request);
			callback.setClient(client);
			invocationRepository.put(request.getSequence(), invocationBean);
			TimelineManager.time(request, Phase.Start);
		}
		InvocationResponse response = null;
		try {
			response = client.write(request, callback);
		} catch (RuntimeException e) {
			invocationRepository.remove(request.getSequence());
			TimelineManager.removeTimeline(request);
			throw new NetException("remote call failed:" + request, e);
		} finally {
			if (response != null) {
				invocationRepository.remove(request.getSequence());
				TimelineManager.removeTimeline(request);
			}
		}
		return response;
	}

	public static InvocationRequest createRemoteCallRequest(InvokerContext invocationContext,
			InvokerConfig<?> invokerConfig) {
		InvocationRequest request = invocationContext.getRequest();
		if (request == null) {
			request = SerializerFactory.getSerializer(invokerConfig.getSerialize()).newRequest(invocationContext);
			invocationContext.setRequest(request);
		}
		request.setSequence(requestSequenceMaker.incrementAndGet() * -1);
		return request;
	}

	public static InvocationResponse createNoReturnResponse() {
		return new NoReturnResponse();
	}

	public static boolean isHeartErrorResponse(InvocationResponse response) {
		try {
			return response != null && response.getMessageType() == Constants.MESSAGE_TYPE_HEART
					&& response.getCause() != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static Throwable toInvocationThrowable(Object responseReturn) {
		if (responseReturn == null) {
			return null;
		} else if (responseReturn instanceof Throwable) {
			return (Throwable) responseReturn;
		} else if (responseReturn instanceof Map) {
			Map errors = (Map) responseReturn;
			String detailMessage = (String) errors.get("detailMessage");
			StackTraceElement[] stackTrace = (StackTraceElement[]) errors.get("stackTrace");
			DPSFException e = new DPSFException(detailMessage);
			e.setStackTrace(stackTrace);
			return e;
		} else {
			return new DPSFException(responseReturn.toString());
		}
	}

	static class NoReturnResponse implements InvocationResponse {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 4348389641787057819L;

		private long invokerRequestTime;

		private long invokerResponseTime;

		private long providerRequestTime;

		private long providerResponseTime;

		public long getInvokerRequestTime() {
			return invokerRequestTime;
		}

		public void setInvokerRequestTime(long invokerRequestTime) {
			this.invokerRequestTime = invokerRequestTime;
		}

		public long getInvokerResponseTime() {
			return invokerResponseTime;
		}

		public void setInvokerResponseTime(long invokerResponseTime) {
			this.invokerResponseTime = invokerResponseTime;
		}

		public long getProviderRequestTime() {
			return providerRequestTime;
		}

		public void setProviderRequestTime(long providerRequestTime) {
			this.providerRequestTime = providerRequestTime;
		}

		public long getProviderResponseTime() {
			return providerResponseTime;
		}

		public void setProviderResponseTime(long providerResponseTime) {
			this.providerResponseTime = providerResponseTime;
		}

		@Override
		public void setMessageType(int messageType) {
		}

		@Override
		public int getMessageType() {
			return 0;
		}

		@Override
		public String getCause() {
			return null;
		}

		@Override
		public Object getReturn() {
			return null;
		}

		@Override
		public void setReturn(Object obj) {
		}

		@Override
		public byte getSerialize() {
			return 0;
		}

		@Override
		public void setSequence(long seq) {
		}

		@Override
		public long getSequence() {
			return 0;
		}

		@Override
		public Object getObject() {
			return null;
		}

		@Override
		public Object getContext() {
			return null;
		}

		@Override
		public void setContext(Object context) {
		}

		@Override
		public void setSerialize(byte serialize) {
		}
	}

}
