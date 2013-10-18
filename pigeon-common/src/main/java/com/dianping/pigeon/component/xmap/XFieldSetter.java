package com.dianping.pigeon.component.xmap;

import java.lang.reflect.Field;

public class XFieldSetter implements XSetter {

	/** field */
	private final Field field;

	/**
	 * 
	 * 
	 * @param field
	 *            <code>Field</code>
	 */
	public XFieldSetter(Field field) {
		this.field = field;
		this.field.setAccessible(true);
	}

	public Class<?> getType() {
		return field.getType();
	}

	public void setValue(Object instance, Object value) throws IllegalAccessException {
		field.set(instance, value);
	}
}
