package com.dianping.pigeon.component.xmap;

import java.net.URL;
import java.util.ArrayList;

public class Context extends ArrayList<Object> {

	private static final long serialVersionUID = 1L;

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return Thread.currentThread().getContextClassLoader().loadClass(className);
	}

	public URL getResource(String name) {
		return Thread.currentThread().getContextClassLoader().getResource(name);
	}

	public Object getObject() {
		int size = size();
		if (size > 0) {
			return get(size - 1);
		}
		return null;
	}

	public Object getParent() {
		int size = size();
		if (size > 1) {
			return get(size - 2);
		}
		return null;
	}

	public void push(Object object) {
		add(object);
	}

	public Object pop() {
		int size = size();
		if (size > 0) {
			return remove(size - 1);
		}
		return null;
	}

}
