/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import org.apache.commons.lang.SerializationException;

import com.dianping.pigeon.remoting.common.codec.Serializer;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
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
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		return new ProtobufRequest(request);
	}

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		MessageLite response;
		try {
			response = responsePrototype.newBuilderForType().mergeFrom(is).build();
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		return new ProtobufResponse(response);
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		serializeResponse(os, obj);
	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		if (!(obj instanceof MessageLite)) {
			return;
		}
		byte[] bytes = ((MessageLite) obj).toByteArray();
		try {
			os.write(bytes);
		} catch (IOException e) {
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
			Class<?> objType = Class.forName(stubName);
			Constructor<?> constructor = objType.getDeclaredConstructor(channelClass);
			constructor.setAccessible(true);
			return constructor.newInstance(channel);
		} catch (Exception e) {
			throw new SerializationException(e);
		}

		// return
		// Proxy.newProxyInstance(ProxyBeanFactory.class.getClassLoader(), new
		// Class[] { invokerConfig
		// .getServiceInterface() },
		// new ServiceInvocationProxy(invokerConfig,
		// InvocationHandlerFactory.createInvokeHandler(invokerConfig)));
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
