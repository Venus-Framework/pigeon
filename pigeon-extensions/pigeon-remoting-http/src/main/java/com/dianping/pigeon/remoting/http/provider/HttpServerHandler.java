/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.remoting.http.adapter.HttpAdapter;
import com.dianping.pigeon.remoting.http.adapter.HttpAdapterFactory;
import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.log.LoggerLoader;

import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.remoting.common.codec.Serializer;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.http.HttpUtils;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.domain.DefaultProviderContext;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

public class HttpServerHandler implements HttpHandler {

	protected final Logger logger = LoggerLoader.getLogger(this.getClass());

	private Server server;

	protected static Map<Long, HttpCallbackFuture> callbacks = new ConcurrentHashMap<Long, HttpCallbackFuture>();

	public HttpServerHandler(Server server) {
		this.server = server;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long createTime = System.currentTimeMillis();
		Object obj;
		String customize = request.getParameter("customize");
		if("true".equalsIgnoreCase(customize)) {
			HttpAdapter httpAdapter = ExtensionLoader.getExtension(HttpAdapter.class);
			if(httpAdapter != null) {
				obj = HttpUtils.createDefaultRequest(httpAdapter.convert(request));
			} else {
				throw new IllegalArgumentException("ServiceLoader httpAdapter not found!");
			}
		} else if("service".equalsIgnoreCase(customize)) {
			String serviceName = request.getParameter("url");
			if(StringUtils.isNotBlank(serviceName)) {
				HttpAdapter httpAdapter = HttpAdapterFactory.getHttpAdapters().get(serviceName);
				if(httpAdapter != null) {
					obj = HttpUtils.createDefaultRequest(httpAdapter.convert(request));
				} else {
					throw new IllegalArgumentException("HttpAdapter not found!");
				}
			} else {
				throw new IllegalArgumentException("serviceName can't be blank!");
			}
		} else {
			String serialize = request.getParameter("serialize");
			if (serialize == null) {
				serialize = request.getHeader("serialize");
			}
			if (StringUtils.isBlank(serialize)) {
				response.setStatus(200);
				return;
			}

			if (!request.getMethod().equalsIgnoreCase("POST")) {
				if (SerializerFactory.SERIALIZE_JSON == Byte.parseByte(serialize)) {
					response.setStatus(200);
				} else {
					response.setStatus(500);
				}
				return;

			} else {
				Serializer serializer = SerializerFactory.getSerializer(Byte.parseByte(serialize));
				obj = serializer.deserializeRequest(request.getInputStream());
			}
		}

		InvocationRequest invocationRequest;
		if (!(obj instanceof InvocationRequest)) {
			throw new IllegalArgumentException("invalid request type:" + obj.getClass());
		} else {
			invocationRequest = (InvocationRequest) obj;
		}
		invocationRequest.getParameters();
		invocationRequest.setServiceName(HttpUtils.getDefaultServiceUrl(invocationRequest.getServiceName()));
		invocationRequest.setCreateMillisTime(createTime);
		ProviderContext invocationContext = new DefaultProviderContext(invocationRequest, new HttpChannel(request,
				response));
		Future<InvocationResponse> invocationResponse = null;
		try {
			callbacks.put(invocationRequest.getSequence(), new HttpCallbackFuture(invocationRequest, invocationContext));

			invocationResponse = server.processRequest(invocationRequest, invocationContext);
			if (invocationResponse != null) {
				if(Constants.REPLY_MANUAL) {
					callbacks.get(invocationRequest.getSequence()).get(invocationRequest.getTimeout());
				}
				invocationResponse.get();
			}
		} catch (Throwable e) {
			if (invocationResponse != null && !invocationResponse.isCancelled()) {
				invocationResponse.cancel(true);
			}
			String msg = "process http request[" + request.getRemoteAddr() + "] failed:" + invocationRequest;
			// 心跳消息只返回正常的, 异常不返回
			if (invocationRequest.getCallType() == Constants.CALLTYPE_REPLY
					&& invocationRequest.getMessageType() != Constants.MESSAGE_TYPE_HEART) {
				invocationContext.getChannel().write(ProviderUtils.createFailResponse(invocationRequest, e));
				logger.error(msg, e);
			}
		} finally {
			callbacks.remove(invocationRequest.getSequence());
		}
	}

}