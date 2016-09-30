package com.dianping.pigeon.remoting.invoker.util;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.exception.ApplicationException;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.concurrent.Callback;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.domain.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

public class InvokerUtils {

	private static ServiceInvocationRepository invocationRepository = ServiceInvocationRepository.getInstance();

	private static final Logger logger = LoggerLoader.getLogger(InvokerUtils.class);

	public static InvocationResponse sendRequest(Client client, InvocationRequest request, Callback callback) {
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			RemoteInvocationBean invocationBean = new RemoteInvocationBean();
			invocationBean.request = request;
			invocationBean.callback = callback;
			callback.setRequest(request);
			callback.setClient(client);
			invocationRepository.put(request.getSequence(), invocationBean);
		}
		InvocationResponse response = null;
		try {
			response = client.write(request, callback);
		} catch (NetworkException e) {
			invocationRepository.remove(request.getSequence());
			logger.warn("network exception ocurred:" + request, e);
			throw e;
		} finally {
			if (response != null) {
				invocationRepository.remove(request.getSequence());
			}
		}
		return response;
	}

	public static InvocationRequest createRemoteCallRequest(InvokerContext invokerContext,
			InvokerConfig<?> invokerConfig) {
		InvocationRequest request = invokerContext.getRequest();
		if (request == null) {
			if (invokerConfig.getSerialize() == SerializerFactory.SERIALIZE_THRIFT) {
				request = new GenericRequest(invokerContext);
			} else {
				request = InvocationUtils.newRequest(invokerContext);
			}
			invokerContext.setRequest(request);
		}
		return request;
	}

	public static InvocationResponse createNoReturnResponse() {
		return new NoReturnResponse();
	}

	public static InvocationResponse createDefaultResponse(Object defaultResult) {
		return new NoReturnResponse(defaultResult);
	}

	public static InvocationResponse createThrowableResponse(Throwable e) {
		InvocationResponse response = new NoReturnResponse(e);
		response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
		return response;
	}

	public static InvocationResponse createFutureResponse(Future serviceFuture) {
		FutureResponse resp = new FutureResponse();
		resp.setServiceFuture(serviceFuture);
		return resp;
	}

	public static boolean isHeartErrorResponse(InvocationResponse response) {
		return response != null && response.getMessageType() == Constants.MESSAGE_TYPE_HEART
				&& response.getCause() != null;
	}

	public static RuntimeException toApplicationRuntimeException(InvocationResponse response) {
		Throwable t = toApplicationException(response);
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		} else {
			return new ApplicationException(t);
		}
	}

	public static Exception toApplicationException(InvocationResponse response) {
		Object responseReturn = response.getReturn();
		if (responseReturn == null) {
			return new ApplicationException(response.getCause());
		} else if (responseReturn instanceof RpcException) {
			return new ApplicationException((RpcException) responseReturn);
		} else if (responseReturn instanceof Exception) {
			return (Exception) responseReturn;
		} else if (responseReturn instanceof Throwable) {
			return new RemoteInvocationException((Throwable) responseReturn);
		} else if (responseReturn instanceof Map) {
			Map errors = (Map) responseReturn;
			String detailMessage = (String) errors.get("detailMessage");
			StackTraceElement[] stackTrace = (StackTraceElement[]) errors.get("stackTrace");
			ApplicationException e = new ApplicationException(detailMessage);
			e.setStackTrace(stackTrace);
			return e;
		} else {
			return new ApplicationException(responseReturn.toString());
		}
	}

	public static RpcException toRpcException(InvocationResponse response) {
		Throwable e = null;
		Object responseReturn = response.getReturn();
		if (responseReturn == null) {
			return new RemoteInvocationException(response.getCause());
		} else if (responseReturn instanceof Throwable) {
			e = (Throwable) responseReturn;
		} else if (responseReturn instanceof Map) {
			Map errors = (Map) responseReturn;
			String detailMessage = (String) errors.get("detailMessage");
			StackTraceElement[] stackTrace = (StackTraceElement[]) errors.get("stackTrace");
			e = new RemoteInvocationException(detailMessage);
			e.setStackTrace(stackTrace);
		} else {
			e = new RemoteInvocationException(responseReturn.toString());
		}
		if (!(e instanceof RpcException)) {
			return new RemoteInvocationException(e);
		}
		return (RpcException) e;
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

		private Object result;

		private int messageType = Constants.MESSAGE_TYPE_SERVICE;

		public NoReturnResponse() {
		}

		public NoReturnResponse(Object defaultResult) {
			this.result = defaultResult;
		}

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
			this.messageType = messageType;
		}

		@Override
		public int getMessageType() {
			return messageType;
		}

		@Override
		public String getCause() {
			return null;
		}

		@Override
		public Object getReturn() {
			return result;
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

		@Override
		public void setSize(int size) {
		}

		@Override
		public int getSize() {
			return 0;
		}

		@Override
		public Map<String, Serializable> getResponseValues() {
			return null;
		}

		@Override
		public void setResponseValues(Map<String, Serializable> responseValues) {

		}

		@Override
		public long getCreateMillisTime() {
			return 0;
		}

		@Override
		public void setCreateMillisTime(long createMillisTime) {

		}
	}

	public static class FutureResponse implements InvocationResponse {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 4348389641787057819L;

		private long invokerRequestTime;

		private long invokerResponseTime;

		private long providerRequestTime;

		private long providerResponseTime;

		private Future serviceFuture;

		public Future getServiceFuture() {
			return serviceFuture;
		}

		public void setServiceFuture(Future serviceFuture) {
			this.serviceFuture = serviceFuture;
		}

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
			return Constants.MESSAGE_TYPE_SERVICE;
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

		@Override
		public void setSize(int size) {
		}

		@Override
		public int getSize() {
			return 0;
		}

		@Override
		public Map<String, Serializable> getResponseValues() {
			return null;
		}

		@Override
		public void setResponseValues(Map<String, Serializable> responseValues) {

		}

		@Override
		public long getCreateMillisTime() {
			return 0;
		}

		@Override
		public void setCreateMillisTime(long createMillisTime) {

		}
	}
}
