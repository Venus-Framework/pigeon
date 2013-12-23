/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ServiceMethod {

	private Method method;

	private Object service;

	private Class<?>[] parameterClasses;

	public Class<?>[] getParameterClasses() {
		return parameterClasses;
	}

	public void setParameterClasses(Class<?>[] parameterClasses) {
		this.parameterClasses = parameterClasses;
	}

	private int parameterLength;

	public ServiceMethod(Object service, Method method) {

		this.service = service;
		this.method = method;
		this.parameterClasses = regulateTypes(this.method.getParameterTypes());
		this.parameterLength = this.parameterClasses.length;
	}

	private Class<?>[] regulateTypes(Class<?>[] types) {

		for (int i = 0; i < types.length; i++) {
			if (types[i] == byte.class) {
				types[i] = Byte.class;
			} else if (types[i] == short.class) {
				types[i] = Short.class;
			} else if (types[i] == int.class) {
				types[i] = Integer.class;
			} else if (types[i] == boolean.class) {
				types[i] = Boolean.class;
			} else if (types[i] == long.class) {
				types[i] = Long.class;
			} else if (types[i] == float.class) {
				types[i] = Float.class;
			} else if (types[i] == double.class) {
				types[i] = Double.class;
			}
		}
		return types;
	}

	public int getParameterSize() {
		return this.parameterLength;
	}

	public Method getMethod() {
		return this.method;
	}

	/**
	 * @return the service
	 */
	public Object getService() {
		return service;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public Object invoke(Object[] arguments) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return this.getMethod().invoke(this.getService(), arguments);
	}
}
