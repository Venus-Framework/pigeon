/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.dianping.dpsf.component.DPSFRequest;
import com.dianping.dpsf.component.DPSFResponse;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.component.RemoteServiceError;
import com.dianping.pigeon.serialize.SerializerFactory;

public final class ResponseUtils {

	private ResponseUtils() {
	}

	public static DPSFResponse createThrowableResponse(long seq, byte serialization, Throwable e) {

		DPSFResponse response = null;
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

	public static DPSFResponse createFailResponse(DPSFRequest request, Throwable e) {
		DPSFResponse response = null;
		byte serialization = request.getSerializ();
		if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			response = new DefaultResponse(serialization, request.getSequence(), Constants.MESSAGE_TYPE_HEART, e);
		} else {
			response = createThrowableResponse(request.getSequence(), request.getSerializ(), e);
		}
		return response;
	}

	public static DPSFResponse createServiceExceptionResponse(DPSFRequest request, Throwable e) {
		DPSFResponse response = null;
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

	public static DPSFResponse createSuccessResponse(DPSFRequest request, Object returnObj) {
		DPSFResponse response = null;
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

	public static DPSFResponse createHeartResponse(DPSFRequest request) {
		DPSFResponse response = new DefaultResponse(Constants.MESSAGE_TYPE_HEART, request.getSerializ());
		response.setSequence(request.getSequence());
		response.setReturn(Constants.VERSION_150);
		return response;
	}

	private static String extractStackTrace(Throwable t) {
		StringWriter me = new StringWriter();
		PrintWriter pw = new PrintWriter(me);
		t.printStackTrace(pw);
		pw.flush();
		return me.toString();
	}

	public static boolean isHeartErrorResponse(DPSFResponse response) {
		try {
			return response != null && response.getMessageType() == Constants.MESSAGE_TYPE_HEART
					&& response.getCause() != null;
		} catch (Exception e) {
			return false;
		}
	}

}
