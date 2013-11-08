/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.component.context.DefaultInvokerContext;

public class ServiceInvocationProxy implements InvocationHandler {

	private static final Logger logger = Log4jLoader.getLogger(ServiceInvocationProxy.class);
	private InvokerMetaData metaData;
	private ServiceInvocationHandler handler;

	public ServiceInvocationProxy(InvokerMetaData metaData, ServiceInvocationHandler handler) {
		this.metaData = metaData;
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
		return extractResult(handler.handle(new DefaultInvokerContext(metaData, method, args)), method.getReturnType());

	}

	/**
	 * 
	 * 
	 * @param response
	 * @param returnType
	 * @return
	 * @throws Throwable
	 */
	private Object extractResult(InvocationResponse response, Class<?> returnType) throws Throwable {
		Object responseReturn = response.getReturn();
		if (responseReturn != null) {
			int messageType = response.getMessageType();
			if (messageType == Constants.MESSAGE_TYPE_SERVICE) {
				return responseReturn;
			} else if (messageType == Constants.MESSAGE_TYPE_EXCEPTION
					|| messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				
				throw InvocationUtils.toInvocationThrowable(responseReturn);
			}
			throw new RuntimeException("Unsupported response to extract result with type[" + messageType + "].");
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
