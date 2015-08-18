/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.service.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.dianping.pigeon.remoting.common.util.InvocationUtils;

public class ServiceMethod {

	private Method method;

	private Object service;

	private Class<?>[] originalParameterClasses;

	private Class<?>[] parameterClasses;

	private boolean needCastParameterClasses = false;

	public boolean isNeedCastParameterClasses() {
		return needCastParameterClasses;
	}

	public void setNeedCastParameterClasses(boolean needCastParameterClasses) {
		this.needCastParameterClasses = needCastParameterClasses;
	}

	public Class<?>[] getParameterClasses() {
		return parameterClasses;
	}

	public Class<?>[] getOriginalParameterClasses() {
		return originalParameterClasses;
	}

	public void setParameterClasses(Class<?>[] parameterClasses) {
		this.parameterClasses = parameterClasses;
	}

	private int parameterLength;

	public ServiceMethod(Object service, Method method) {
		this.service = service;
		this.method = method;
		this.parameterClasses = regulateTypes(this.method.getParameterTypes());
		this.originalParameterClasses = this.method.getParameterTypes();
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
		if (needCastParameterClasses && arguments != null && parameterClasses != null) {
			if (parameterClasses.length == arguments.length) {
				Object[] argumentsCast = new Object[arguments.length];
				for (int i = 0; i < arguments.length; i++) {
					Object arg = arguments[i];
					if (arg != null) {
						Class<?> argClass = arg.getClass();
						if (argClass != parameterClasses[i]) {
							if (argClass.equals(Double.class) && parameterClasses[i].equals(Float.class)) {
								arg = ((Double) arg).floatValue();
							} else if (argClass.equals(Integer.class) && parameterClasses[i].equals(Short.class)) {
								arg = ((Integer) arg).shortValue();
							}
						}
					}
					argumentsCast[i] = arg;
				}
				try {
					return this.getMethod().invoke(this.getService(), argumentsCast);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("invalid parameter types:"
							+ InvocationUtils.getRemoteCallFullName(this.getMethod().getName(), argumentsCast),
							e.getCause());
				}
			}
		}
		try {
			return this.getMethod().invoke(this.getService(), arguments);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("invalid parameter types:"
					+ InvocationUtils.getRemoteCallFullName(this.getMethod().getName(), arguments), e.getCause());
		}
	}
}
