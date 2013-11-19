/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.common.component.RemoteServiceError;
import com.dianping.pigeon.serialize.SerializerFactory;

public final class ResponseUtils {

	private ResponseUtils() {
	}

	public static InvocationResponse createThrowableResponse(long seq, byte serialization, Throwable e) {

		InvocationResponse response = null;
		switch (serialization) {
		case SerializerFactory.SERIALIZE_JAVA:
			response = new DefaultResponse(serialization, seq, Constants.MESSAGE_TYPE_EXCEPTION, e);
			break;
		case SerializerFactory.SERIALIZE_HESSIAN:
			response = new DefaultResponse(serialization, seq, Constants.MESSAGE_TYPE_EXCEPTION, e);
			break;
		}
		return response;
	}

	public static InvocationResponse createFailResponse(InvocationRequest request, Throwable e) {
		InvocationResponse response = null;
		byte serialization = request.getSerializ();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			response = new DefaultResponse(serialization, request.getSequence(), Constants.MESSAGE_TYPE_HEART, e);
		} else {
			response = createThrowableResponse(request.getSequence(), request.getSerializ(), e);
		}
		return response;
	}

	public static InvocationResponse createServiceExceptionResponse(InvocationRequest request, Throwable e) {
		InvocationResponse response = null;
		byte serialization = request.getSerializ();
		switch (serialization) {
		case SerializerFactory.SERIALIZE_JAVA:
			response = new DefaultResponse(serialization, request.getSequence(),
					Constants.MESSAGE_TYPE_SERVICE_EXCEPTION, e);
			break;
		case SerializerFactory.SERIALIZE_HESSIAN:
			response = new DefaultResponse(serialization, request.getSequence(),
					Constants.MESSAGE_TYPE_SERVICE_EXCEPTION, e);
			break;
		case SerializerFactory.SERIALIZE_HESSIAN1:
			String stackTrace = "UnknownTrace";
			stackTrace = extractStackTrace(e);
			RemoteServiceError serviceException = new RemoteServiceError(e.getClass().getName(), e.getMessage(),
					stackTrace);
			response = new DefaultResponse(serialization, request.getSequence(),
					Constants.MESSAGE_TYPE_SERVICE_EXCEPTION, serviceException);
			break;
		}
		return response;
	}

	public static InvocationResponse createSuccessResponse(InvocationRequest request, Object returnObj) {
		InvocationResponse response = null;
		byte serialization = request.getSerializ();
		switch (serialization) {
		case SerializerFactory.SERIALIZE_JAVA:
			response = new DefaultResponse(serialization, request.getSequence(), Constants.MESSAGE_TYPE_SERVICE,
					returnObj);
			break;
		case SerializerFactory.SERIALIZE_HESSIAN:
		case SerializerFactory.SERIALIZE_HESSIAN1:
			response = new DefaultResponse(serialization, request.getSequence(), Constants.MESSAGE_TYPE_SERVICE,
					returnObj);
			break;
		}
		return response;
	}

	public static InvocationResponse createHeartResponse(InvocationRequest request) {
		InvocationResponse response = new DefaultResponse(Constants.MESSAGE_TYPE_HEART, request.getSerializ());
		response.setSequence(request.getSequence());
		response.setReturn(Constants.VERSION_150);
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
		public byte getSerializ() {
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
	}
}
