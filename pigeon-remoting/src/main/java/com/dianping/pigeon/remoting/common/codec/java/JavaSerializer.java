/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.java;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.SerializationException;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.dpsf.spring.ProxyBeanFactory;
import com.dianping.pigeon.remoting.common.codec.Serializer;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationRequest;
import com.dianping.pigeon.remoting.common.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.process.InvocationHandlerFactory;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationProxy;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class JavaSerializer implements Serializer {

	private static ClassLoader classLoader = JavaSerializer.class.getClassLoader();

	@Override
	public Object deserializeResponse(InputStream is) throws SerializationException {
		return deserializeRequest(is);
	}

	@Override
	public Object deserializeRequest(InputStream is) throws SerializationException {
		CompactObjectInputStream coin;
		try {
			coin = new CompactObjectInputStream(is, classLoader);
			try {
				return coin.readObject();
			} finally {
				coin.close();
			}
		} catch (Throwable t) {
			throw new SerializationException(t);
		}

	}

	@Override
	public void serializeResponse(OutputStream os, Object obj) throws SerializationException {
		serializeRequest(os, obj);
	}

	@Override
	public void serializeRequest(OutputStream os, Object obj) throws SerializationException {
		try {
			ObjectOutputStream oout = new CompactObjectOutputStream(os);
			try {
				oout.writeObject(obj);
				oout.flush();
			} finally {
				oout.close();
			}
		} catch (Throwable t) {
			throw new SerializationException(t);
		}
	}

	@Override
	public Object proxyRequest(InvokerConfig<?> invokerConfig) throws SerializationException {
		return Proxy.newProxyInstance(ProxyBeanFactory.class.getClassLoader(), new Class[] { invokerConfig
				.getServiceInterface() },
				new ServiceInvocationProxy(invokerConfig, InvocationHandlerFactory.createInvokeHandler(invokerConfig)));
	}

	@Override
	public InvocationResponse newResponse() throws SerializationException {
		return new DefaultResponse();
	}

	@Override
	public InvocationRequest newRequest(InvokerContext invokerContext) throws SerializationException {
		return new DefaultRequest(invokerContext);
	}

}
