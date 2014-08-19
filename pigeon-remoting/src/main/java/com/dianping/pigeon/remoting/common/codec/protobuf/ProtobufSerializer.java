/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import org.apache.commons.lang.ClassUtils;

import com.dianping.pigeon.remoting.common.codec.Serializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.google.protobuf.MessageLite;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class ProtobufSerializer implements Serializer {

	private final MessageLite requestPrototype = ProtobufRpcProtos.Request.getDefaultInstance();
	private final MessageLite responsePrototype = ProtobufRpcProtos.Response.getDefaultInstance();

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		MessageLite request;
		try {
			request = requestPrototype.newBuilderForType().mergeFrom(is).build();
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
		return new ProtobufRequest(request);
	}

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		MessageLite response;
		try {
			response = responsePrototype.newBuilderForType().mergeFrom(is).build();
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
		return new ProtobufResponse(response);
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		if (obj instanceof ProtobufRequest) {
			serializeResponse(os, ((ProtobufRequest) obj).getObject());
		} else {
			serializeResponse(os, obj);
		}
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		Object msg = obj;
		if (obj instanceof ProtobufResponse) {
			msg = ((ProtobufResponse) obj).getObject();
		}
		if (!(msg instanceof MessageLite)) {
			throw new SerializationException("invalid format:" + obj);
		}
		byte[] bytes = ((MessageLite) msg).toByteArray();
		try {
			os.write(bytes);
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public Object proxyRequest(InvokerConfig<?> invokerConfig) throws SerializationException {
		String iface = invokerConfig.getServiceInterface().getName();
		if (iface.endsWith("$BlockingInterface")) {
			iface = iface.substring(0, iface.lastIndexOf("$BlockingInterface"));
		}
		String stubName = iface + "$BlockingStub";
		Class<?> channelClass = com.google.protobuf.BlockingRpcChannel.class;
		Object channel = new ProtobufBlockingRpcChannel(invokerConfig);
		try {
			Class<?> objType = ClassUtils.getClass(stubName);
			Constructor<?> constructor = objType.getDeclaredConstructor(channelClass);
			constructor.setAccessible(true);
			return constructor.newInstance(channel);
		} catch (Throwable e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public InvocationResponse newResponse() throws SerializationException {
		return new ProtobufResponse();
	}

	@Override
	public InvocationRequest newRequest(InvokerContext invokerContext) throws SerializationException {
		return new ProtobufRequest(invokerContext);
	}

}
