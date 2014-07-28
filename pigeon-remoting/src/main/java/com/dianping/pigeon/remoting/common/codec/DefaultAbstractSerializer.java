/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec;

import java.lang.reflect.Proxy;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.process.InvokerProcessHandlerFactory;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationProxy;
import com.dianping.pigeon.util.ClassUtils;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public abstract class DefaultAbstractSerializer implements Serializer {

	@Override
	public Object proxyRequest(InvokerConfig<?> invokerConfig) throws SerializationException {
		return Proxy.newProxyInstance(ClassUtils.getCurrentClassLoader(invokerConfig.getClassLoader()),
				new Class[] { invokerConfig.getServiceInterface() }, new ServiceInvocationProxy(invokerConfig,
						InvokerProcessHandlerFactory.selectInvocationHandler(invokerConfig)));
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
