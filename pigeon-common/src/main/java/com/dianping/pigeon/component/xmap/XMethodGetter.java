package com.dianping.pigeon.component.xmap;

import java.lang.reflect.Method;

public class XMethodGetter implements XGetter {

	/** method */
	private Method method;

	/** class */
	private Class<?> clazz;

	/** bean name */
	private String name;

	/**
	 * 
	 * 
	 * @param method
	 *            <code>Method</code>
	 * @param clazz
	 *            <code>Class</code>
	 * @param name
	 *            bean name
	 */
	public XMethodGetter(Method method, Class<?> clazz, String name) {
		this.method = method;
		this.clazz = clazz;
		this.name = name;
	}

	/**
	 * @see com.alipay.sofa.common.xmap.XGetter#getType()
	 */
	public Class<?> getType() {
		if (method == null) {
			throw new IllegalArgumentException("no such getter method: " + clazz.getName() + '.' + name);
		}

		return method.getReturnType();
	}

	public Object getValue(Object instance) throws Exception {
		if (method == null) {
			throw new IllegalArgumentException("no such getter method: " + clazz.getName() + '.' + name);
		}

		if (instance == null) {
			return null;
		}
		return method.invoke(instance, new Object[0]);
	}

}
