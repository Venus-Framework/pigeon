/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.domain;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.domain.AbstractInvocationContext;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;

public class DefaultInvokerContext extends AbstractInvocationContext implements InvokerContext {

	private InvokerConfig<?> invokerConfig;
	private String methodName;
	private Class<?>[] parameterTypes;
	private Object[] arguments;
	private Client client;

	public DefaultInvokerContext(InvokerConfig<?> invokerConfig, String methodName, Class<?>[] parameterTypes,
			Object[] arguments) {
		super(null);
		this.invokerConfig = invokerConfig;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.arguments = arguments;
	}

	public InvokerConfig<?> getInvokerConfig() {
		return invokerConfig;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public Client getClient() {
		return client;
	}

	@Override
	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
