/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.BadResponseException;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.domain.DefaultInvokerContext;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class ServiceInvocationProxy implements InvocationHandler {

	private static final Logger logger = LoggerLoader.getLogger(ServiceInvocationProxy.class);
	private InvokerConfig<?> invokerConfig;
	private ServiceInvocationHandler handler;

	public ServiceInvocationProxy(InvokerConfig<?> invokerConfig, ServiceInvocationHandler handler) {
		this.invokerConfig = invokerConfig;
		this.handler = handler;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (method.getDeclaringClass() == Object.class) {
			return method.invoke(handler, args);
		}
		if ("toString".equals(methodName) && parameterTypes.length == 0) {
			return handler.toString();
		}
		if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
			return handler.hashCode();
		}
		if ("equals".equals(methodName) && parameterTypes.length == 1) {
			return handler.equals(args[0]);
		}
		return extractResult(handler.handle(new DefaultInvokerContext(invokerConfig, methodName, parameterTypes, args)),
				method.getReturnType());
	}

	public Object extractResult(InvocationResponse response, Class<?> returnType) throws Throwable {
		Object responseReturn = response.getReturn();
		if (responseReturn != null) {
			int messageType = response.getMessageType();
			if (messageType == Constants.MESSAGE_TYPE_SERVICE) {
				return responseReturn;
			} else if (messageType == Constants.MESSAGE_TYPE_EXCEPTION) {
				throw InvokerUtils.toRpcException(response);
			} else if (messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				throw InvokerUtils.toApplicationException(response);
			}
			throw new BadResponseException(response.toString());
		}
		return getReturn(returnType);
	}

	private Object getReturn(Class<?> returnType) {
		if (returnType == byte.class) {
			return (byte) 0;
		} else if (returnType == short.class) {
			return (short) 0;
		} else if (returnType == int.class) {
			return 0;
		} else if (returnType == boolean.class) {
			return false;
		} else if (returnType == long.class) {
			return 0l;
		} else if (returnType == float.class) {
			return 0.0f;
		} else if (returnType == double.class) {
			return 0.0d;
		} else {
			return null;
		}
	}

}
