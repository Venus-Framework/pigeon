/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.util;

import java.util.HashMap;
import java.util.Map;

import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.process.ProviderExceptionTranslator;
import com.dianping.pigeon.util.LangUtils;
import com.dianping.pigeon.util.VersionUtils;

public final class ProviderUtils {

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private static ProviderExceptionTranslator exceptionTranslator = new ProviderExceptionTranslator();

	private ProviderUtils() {
	}

	public static InvocationResponse createThrowableResponse(long seq, byte serialization, Throwable e) {
		InvocationResponse response = null;
		response = SerializerFactory.getSerializer(serialization).newResponse();
		response.setSequence(seq);
		response.setSerialize(serialization);
		response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
		if (serialization == SerializerFactory.SERIALIZE_JSON) {
			response.setReturn(LangUtils.getFullStackTrace(e));
		} else {
			response.setReturn(exceptionTranslator.translate(e));
		}
		return response;
	}

	public static InvocationResponse createFailResponse(InvocationRequest request, Throwable e) {
		InvocationResponse response = null;
		if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
			response = new DefaultResponse(request.getSerialize(), request.getSequence(), Constants.MESSAGE_TYPE_HEART,
					exceptionTranslator.translate(e));
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
		if (serialize == SerializerFactory.SERIALIZE_JSON) {
			response.setReturn(LangUtils.getFullStackTrace(e));
		} else {
			response.setReturn(e);
		}

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
		info.put("group", configManager.getGroup());
		info.put("env", configManager.getEnv());
		response.setReturn(info);

		return response;
	}

	public static String getRequestDetailInfo(String title, ProviderContext providerContext, InvocationRequest request) {
		StringBuilder msg = new StringBuilder();
		msg.append(title).append(", from:")
				.append(providerContext.getChannel() == null ? "" : providerContext.getChannel().getRemoteAddress())
				.append(", to:").append(ExtensionLoader.getExtension(ConfigManager.class).getLocalIp())
				.append(", time:").append(System.currentTimeMillis()).append("\r\nrequest:")
				.append(InvocationUtils.toJsonString(request));
		return msg.toString();
	}
}
