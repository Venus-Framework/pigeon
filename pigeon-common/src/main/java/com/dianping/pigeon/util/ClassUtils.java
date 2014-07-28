package com.dianping.pigeon.util;

public class ClassUtils {

	public static Class loadClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		return org.apache.commons.lang.ClassUtils.getClass(classLoader, className);
	}

	public static Class loadClass(String className) throws ClassNotFoundException {
		return loadClass(null, className);
	}

	public static ClassLoader getCurrentClassLoader(ClassLoader classLoader) {
		if(classLoader != null) {
			return classLoader;
		}
		ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
		if (currentLoader != null) {
			return currentLoader;
		}
		return ClassUtils.class.getClassLoader();
	}
}
