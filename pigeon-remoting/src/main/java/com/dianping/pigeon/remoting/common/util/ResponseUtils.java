/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.util.VersionUtils;

public final class ResponseUtils {

	private ResponseUtils() {
	}

	public static InvocationResponse createThrowableResponse(long seq, byte serialization, Throwable e) {
		InvocationResponse response = null;
		response = SerializerFactory.getSerializer(serialization).newResponse();
		response.setSequence(seq);
		response.setSerialize(serialization);
		response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
		response.setReturn(e);

		return response;
	}

	public static InvocationResponse createFailResponse(InvocationRequest request, Throwable e) {
		InvocationResponse response = null;
		byte serialization = request.getSerialize();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			response = new DefaultResponse(serialization, request.getSequence(), Constants.MESSAGE_TYPE_HEART, e);
		} else {
			response = createThrowableResponse(request.getSequence(), request.getSerialize(), e);
		}
		return response;
	}

	public static InvocationResponse createServiceExceptionResponse(InvocationRequest request, Throwable e) {
		InvocationResponse response = null;
		byte serialize = request.getSerialize();
		response = SerializerFactory.getSerializer(serialize).newResponse();
		response.setSequence(request.getSequence());
		response.setSerialize(serialize);
		response.setMessageType(Constants.MESSAGE_TYPE_SERVICE_EXCEPTION);
		response.setReturn(e);

		// switch (serialization) {
		// case SerializerFactory.SERIALIZE_JAVA:
		// response = new DefaultResponse(serialization, request.getSequence(),
		// Constants.MESSAGE_TYPE_SERVICE_EXCEPTION, e);
		// break;
		// case SerializerFactory.SERIALIZE_HESSIAN:
		// response = new DefaultResponse(serialization, request.getSequence(),
		// Constants.MESSAGE_TYPE_SERVICE_EXCEPTION, e);
		// break;
		// case SerializerFactory.SERIALIZE_HESSIAN1:
		// String stackTrace = "UnknownTrace";
		// stackTrace = extractStackTrace(e);
		// RemoteServiceError serviceException = new
		// RemoteServiceError(e.getClass().getName(), e.getMessage(),
		// stackTrace);
		// response = new DefaultResponse(serialization, request.getSequence(),
		// Constants.MESSAGE_TYPE_SERVICE_EXCEPTION, serviceException);
		// break;
		// }
		return response;
	}

	public static InvocationResponse createSuccessResponse(InvocationRequest request, Object returnObj) {
		InvocationResponse response = null;
		byte serialize = request.getSerialize();
		response = SerializerFactory.getSerializer(serialize).newResponse();
		response.setSequence(request.getSequence());
		response.setSerialize(serialize);
		response.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
		response.setReturn(returnObj);

		return response;
	}

	public static InvocationResponse createHeartResponse(InvocationRequest request) {
		InvocationResponse response = new DefaultResponse(Constants.MESSAGE_TYPE_HEART, request.getSerialize());
		response.setSequence(request.getSequence());
		response.setReturn(Constants.VERSION_150);

		return response;
	}
	
	public static InvocationResponse createHealthCheckResponse(InvocationRequest request) {
		InvocationResponse response = new DefaultResponse(Constants.MESSAGE_TYPE_HEALTHCHECK, request.getSerialize());
		response.setSequence(request.getSequence());
		Map<String, Object> info = new HashMap<String, Object>(); 
		info.put("version", VersionUtils.VERSION);
		response.setReturn(info);

		return response;
	}

	public static InvocationResponse createNoReturnResponse() {
		return new NoReturnResponse();
	}

	private static String extractStackTrace(Throwable t) {
		StringWriter me = new StringWriter();
		PrintWriter pw = new PrintWriter(me);
		t.printStackTrace(pw);
		pw.flush();
		return me.toString();
	}

	public static boolean isHeartErrorResponse(InvocationResponse response) {
		try {
			return response != null && response.getMessageType() == Constants.MESSAGE_TYPE_HEART
					&& response.getCause() != null;
		} catch (Exception e) {
			return false;
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
