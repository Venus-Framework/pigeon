package com.dianping.pigeon.component.xmap;

import java.lang.reflect.Field;

public class XFieldGetter implements XGetter {

	/** field */
	private Field field;

	/**
	 * 
	 * 
	 * @param field
	 *            <code>Field</code>
	 */
	public XFieldGetter(Field field) {
		this.field = field;
		this.field.setAccessible(true);
	}

	public Class<?> getType() {
		return field.getType();
	}

	public Object getValue(Object instance) throws Exception {
		if (instance == null) {
			return null;
		}
		return field.get(instance);
	}

}
