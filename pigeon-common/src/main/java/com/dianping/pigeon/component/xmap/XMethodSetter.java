package com.dianping.pigeon.component.xmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XMethodSetter implements XSetter {

	/** method */
	private final Method method;

	public XMethodSetter(Method method) {
		this.method = method;
		this.method.setAccessible(true);
	}

	public Class<?> getType() {
		return method.getParameterTypes()[0];
	}

	public void setValue(Object instance, Object value) throws IllegalAccessException, InvocationTargetException {
		method.invoke(instance, value);
	}

}
