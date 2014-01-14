package com.dianping.pigeon.console.domain;

import java.io.Serializable;

public class ServiceMethod implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5762333205567884762L;

	private String name;

	private Class<?>[] parameterTypes;

	private Class<?> returnType;

	public ServiceMethod() {
	}

	public ServiceMethod(String name, Class<?>[] parameterTypes, Class<?> returnType) {
		this.name = name;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}
}
